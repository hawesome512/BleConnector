package com.hawesome.bleconnector.kit

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.hawesome.bleconnector.ext.has
import com.hawesome.bleconnector.view.BCApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
*   蓝牙通信指令
* */
data class BleCommand(val cmd: ByteArray, var repeat: Int) {

    companion object {
        const val DEFAULT_REPEAT = 3
        const val ALWAYS_REPEAT = Int.MAX_VALUE
    }

    fun onExecuted(result: ReceivedResult) {
        if (repeat == ALWAYS_REPEAT) return
        if (result == ReceivedResult.ERROR) repeat--
        repeat = 0
    }
}

/*
*   监听接收数据
* */
interface BleReceivedCallback {
    fun onSuccess(result: ReceivedResult)
}

/*
* 断线重连监听
* */
interface OnBleDisConnectListener {
    fun onDisConnect()
}

/*
* 蓝牙通信工具类：初始化、扫描、连接、通信
* open()                        打开系统蓝牙
* getDeviceType(:BleDevice)     获取设备类型
* scanForDevices()              获取可用蓝牙设备（挂起）
* connectForResult(:BleDevice)  连接蓝牙(挂起)
* disConnect(:BleDevice)        关闭蓝牙连接
* executeCMD(:List<BleCommand>) 执行指令序列
* bleReceivedCallback           监听数据回调
* */
object BluetoothKit {

    //<editor-fold desc="初始化"
    private const val TAG = "BluetoothKit"

    //士电蓝牙规约:过滤无关设备、服务和特征
    //服务ID
    private const val SERVICE_UUID = "FE60"

    //Write(发送指令给外设蓝牙）
    private const val WRITE_UUID = "FE61"

    //Notify(监听外设返回数据）
    private const val NOTIFY_UUID = "FE62"

    //士电设备约定的蓝牙名称是设备类型/8位产品序列号，e.g.XST-7T/20050001
    private val bleDevicePattern = "/\\d{8}$"

    //蓝牙UUID映射表
    private var uuidMap = mutableMapOf(
        SERVICE_UUID to SERVICE_UUID,
        NOTIFY_UUID to NOTIFY_UUID,
        WRITE_UUID to WRITE_UUID
    )

    private val bleManager = BleManager.getInstance()

    //待写（发送）的蓝牙指令序列
    private val bleCMDList = mutableListOf<BleCommand>()

    //扫码到的士电蓝牙设备
    private val bleDeviceList = mutableListOf<BleDevice>()

    val bleDeviceLiveData = MutableLiveData<List<BleDevice>>()

    //已连接的蓝牙设备，只允许一台外设接入
    private var connectedBleDevice: BleDevice? = null

    //监听数据回调
    var bleReceivedyCallback: BleReceivedCallback = TagKit

    var onDisconnectListener: OnBleDisConnectListener? = null

    init {
        val application = BCApplication.context as Application
        bleManager.init(application)
        bleManager.enableLog(true)
            .setReConnectCount(1, 5000)
            .setOperateTimeout(5000)
    }

    /*
    * 后台直接打开蓝牙(系统会询问用户授权)
    * */
    fun open() = bleManager.enableBluetooth()

    /*
    * 获取设备类型：XST-7T/12345678
    * */
    fun getDeviceType(bleDevice: BleDevice) = bleDevice.name.split('/').first()

    //</editor-fold>

    //<editor-fold desc="扫描">

    suspend fun scanForDevices(): List<BleDevice> {
        bleDeviceList.clear()
        return suspendCoroutine { continuation ->
            bleManager.scan(object : BleScanCallback() {
                override fun onScanStarted(success: Boolean) {
                    Log.i(TAG, "onScanStarted: ")
                }

                override fun onScanning(bleDevice: BleDevice?) {
                    Log.i(TAG, "onScanning: ${bleDevice?.name ?: "Unknown Device"}")
                    if (filterDevice(bleDevice?.name ?: "")) {
                        bleDeviceList.add(bleDevice!!)
                        bleDeviceLiveData.postValue(bleDeviceList)
                    }
                }

                override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                    scanResultList?.let { scanList ->
                        val validList =
                            scanList.filter { !it.name.isNullOrEmpty() && filterDevice(it.name) }
                        bleDeviceList.clear()
                        bleDeviceList.addAll(validList)
                        try {
                            continuation.resume(validList)
                        } catch (exp: Exception) {
                        }
                        return
                    }
                    continuation.resume(listOf())
                }

            })
        }
    }

    //取消扫描
    private fun cancelScan() {
        bleManager.cancelScan()
    }

    private fun filterDevice(deviceName: String): Boolean {
        val regex = Regex(bleDevicePattern)
        return regex.containsMatchIn(deviceName)
    }
    //</editor-fold>

    //<editor-fold desc="连接">
    suspend fun connectForResult(bleDevice: BleDevice): Boolean {
        cancelScan()
        return suspendCoroutine { continuation ->
            bleManager.connect(bleDevice, object : BleGattCallback() {
                override fun onStartConnect() {
                }

                override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                    continuation.resume(false)
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int
                ) {
                    connectedBleDevice = bleDevice
                    updateUUID(gatt)
                    //设置MTU最大值，避免被拆包，默认20太小，有些蓝牙会拒绝设置mtu
                    bleManager.setMtu(bleDevice, 512, object : BleMtuChangedCallback() {
                        override fun onSetMTUFailure(exception: BleException?) {
                            continuation.resume(false)
                            disConnect(bleDevice!!)
                        }

                        override fun onMtuChanged(mtu: Int) {
                            bleNotify()
                            continuation.resume(true)
                        }
                    })
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    connectedBleDevice = null
                    device?.let {
                        bleManager.stopNotify(it, uuidMap[SERVICE_UUID], uuidMap[NOTIFY_UUID])
                    }
                    onDisconnectListener?.onDisConnect()
                }
            })
        }
    }

    fun disConnect(bleDevice: BleDevice) {
        bleManager.disconnect(bleDevice)
    }

    private fun updateUUID(gatt: BluetoothGatt?) {
        gatt?.let { gatt ->
            val service = gatt.services.first { it.uuid has SERVICE_UUID }
            val notify = service.characteristics.first { it.uuid has NOTIFY_UUID }
            val write = service.characteristics.first { it.uuid has WRITE_UUID }
            uuidMap[SERVICE_UUID] = service.uuid.toString()
            uuidMap[NOTIFY_UUID] = notify.uuid.toString()
            uuidMap[WRITE_UUID] = write.uuid.toString()
        }
    }
    //</editor-fold>

    //<editor-fold desc="蓝牙通信：读、写">
    fun executeCMDList(cmds: List<BleCommand>) {
        bleCMDList.clear()
        bleCMDList.addAll(cmds)

        executeNextCMD()
    }

    private fun bleNotify() {
        if (connectedBleDevice == null) return
        bleManager.notify(
            connectedBleDevice!!,
            uuidMap[SERVICE_UUID],
            uuidMap[NOTIFY_UUID],
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                }

                override fun onNotifyFailure(exception: BleException?) {
                }

                //处理监听数据
                override fun onCharacteristicChanged(data: ByteArray?) {
                    //空值判定
                    if (data == null || bleCMDList.firstOrNull() == null) return
                    //处理返回数据
                    val bleCMD0 = bleCMDList[0]
                    val result = ModbusKit.varifyReceivedData(bleCMD0.cmd, data)
                    bleCMD0.onExecuted(result)
                    if (result != ReceivedResult.ERROR) {
                        bleReceivedyCallback.onSuccess(result)
                    }
                    bleCMDList.removeAt(0)
                    if (bleCMD0.repeat > 0) {
                        bleCMDList.add(bleCMD0)
                    }
                    executeNextCMD()
                }
            })
    }

    private fun executeNextCMD() {
        val bleCMD = bleCMDList.firstOrNull()
        if (bleCMD == null || connectedBleDevice == null) return
        bleManager.write(connectedBleDevice!!,
            uuidMap[SERVICE_UUID],
            uuidMap[WRITE_UUID],
            bleCMD.cmd,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                }

                override fun onWriteFailure(exception: BleException?) {
                }
            })
    }
    //</editor-fold>

}
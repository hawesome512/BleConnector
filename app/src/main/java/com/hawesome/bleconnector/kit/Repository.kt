package com.hawesome.bleconnector.kit

import androidx.lifecycle.liveData
import com.clj.fastble.data.BleDevice
import kotlinx.coroutines.Dispatchers

/*
*   仓库类
* */
object Repository {

    /*
    *   获取扫描到相关的蓝牙设备
    * */
    fun getBleDevices() = liveData(Dispatchers.IO) {
        val devices = BluetoothKit.scanForDevices()
        val result = Result.success(devices)
        emit(result)
    }

    /*
    *   连接设备并获取其状态
    * */
    fun connectForResult(bleDevice: BleDevice) = liveData(Dispatchers.IO) {
        val state = BluetoothKit.connectForResult(bleDevice)
        val result = Result.success(state)
        emit(result)
    }

}
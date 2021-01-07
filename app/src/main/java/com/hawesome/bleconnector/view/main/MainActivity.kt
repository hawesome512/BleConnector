package com.hawesome.bleconnector.view.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.data.BleDevice
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.showToast
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResDrawable
import com.hawesome.bleconnector.kit.BluetoothKit
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.view.device.DeviceActivity
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*

/*
* 初始化页面，隐藏状态栏，权限申请
* 订阅蓝牙中心：①发现可用蓝牙列表；②请求链接的蓝牙状态
* 初始化后马上开始扫描蓝牙
* 点击设备列表/扫码连接蓝牙
* 下拉刷新，重新扫描蓝牙设备
* */

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_SCAN = 1
    }

    private val deviceList = mutableListOf<BleDevice>()
    private val deviceAdapter = DeviceAdapter()
    private var connectingProgress: ProgressBar? = null

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //可用设备列表
        deviceRecycler.layoutManager = LinearLayoutManager(this)
        deviceRecycler.adapter = deviceAdapter
        //下拉刷新（扫描）
        swipeRefresh.setColorSchemeResources(R.color.red_500)
        swipeRefresh.setOnRefreshListener {
            scanDevices()
            swipeRefresh.isRefreshing = false
        }
        //隐藏状态栏
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        //权限
        checkBluetoothPermissionsWithPermissionCheck()
        scanButton.setOnClickListener {
            checkCameraPermissionWithPermissionCheck()
        }
        //蓝牙状态订阅
        viewModel.bleDeviceList.observe(this) { result ->
            deviceList.clear()
            val devices = result.getOrNull()
            if (!devices.isNullOrEmpty()) {
                deviceList.addAll(devices)
                scanButton.isEnabled = true
            }
            deviceAdapter.notifyDataSetChanged()
            scanProgress.visibility = View.INVISIBLE
        }
        viewModel.connectResult.observe(this@MainActivity) { result ->
            if (result.getOrNull() ?: false) {
                //连接成功:初始化工具类，跳转设备页
                val device = viewModel.connectLiveData.value!!
                val type = BluetoothKit.getDeviceType(device)
                DeviceKit.onConnected(type)?.let { TagKit.onConnected(it) }
                startActivity<DeviceActivity>(this@MainActivity) {
                    this.putExtra(DeviceActivity.EXT_DEVICE, device)
                }
            } else {
                R.string.bluetooth_connect_failure.showToast()
            }
            connectingProgress?.visibility = View.INVISIBLE
        }
        scanDevices()
    }

    private fun scanDevices() {
        viewModel.refresh()
        scanProgress.visibility = View.VISIBLE
        scanButton.isEnabled = false
    }

    @NeedsPermission(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun checkBluetoothPermissions() {
        BluetoothKit.open()
        val tintColor = getColor(R.color.bluetooth_on)
        bluetoothText.setText(R.string.bluetooth_on)
        bluetoothText.setTextColor(tintColor)
        bluetoothImage.drawable.setTint(tintColor)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.VIBRATE)
    fun checkCameraPermission() {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, REQUEST_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_SCAN || data == null) return
        val bundle: Bundle = data.getExtras() ?: return
        if (bundle.getInt(CodeUtils.RESULT_TYPE) != CodeUtils.RESULT_SUCCESS) return
        bundle.getString(CodeUtils.RESULT_STRING)?.let { info ->
            deviceList.firstOrNull { info.contains(it.name) }?.let {
                connect(it,scanProgress)
            }
        }
    }

    private fun connect(bleDevice: BleDevice,progressBar: ProgressBar){
        viewModel.connect(bleDevice)
        progressBar.visibility = View.VISIBLE
        connectingProgress = progressBar
    }

    inner class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val deviceImage = itemView.findViewById<ImageView>(R.id.deviceImage)
            val deviceName = itemView.findViewById<TextView>(R.id.deviceName)
            val deviceProgress = itemView.findViewById<ProgressBar>(R.id.deviceProgress)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            val holder = ViewHolder(itemView)
            holder.itemView.setOnClickListener {
                val device = deviceList[holder.adapterPosition]
                connect(device,holder.deviceProgress)
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bleDevice = deviceList[position]
            val deviceType = BluetoothKit.getDeviceType(bleDevice)
            holder.deviceImage.setImageDrawable(deviceType.toResDrawable())
            holder.deviceName.setText(bleDevice.name)
        }

        override fun getItemCount() = deviceList.size
    }

}
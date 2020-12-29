package com.hawesome.bleconnector.view.main

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.data.BleDevice
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResDrawable
import com.hawesome.bleconnector.kit.BluetoothKit
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.view.device.DeviceActivity
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    private val deviceList = mutableListOf<BleDevice>()
    private val deviceAdapter = DeviceAdapter()

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        checkBluetoothPermissionsWithPermissionCheck()
        deviceRecycler.layoutManager = LinearLayoutManager(this)
        deviceRecycler.adapter = deviceAdapter
        viewModel.bleDeviceList.observe(this) { result ->
            deviceList.clear()
            result.getOrNull()?.let {
                deviceList.addAll(it)
                scanButton.isEnabled = true
            }
            deviceAdapter.notifyDataSetChanged()
            scanProgress.visibility = View.INVISIBLE
        }
        swipeRefresh.setColorSchemeResources(R.color.red_500)
        swipeRefresh.setOnRefreshListener {
            refresh()
            swipeRefresh.isRefreshing = false
        }
        refresh()
    }

    private fun refresh() {
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
                viewModel.connect(device)
                holder.deviceProgress.visibility = View.VISIBLE
            }
            viewModel.connectResult.observe(this@MainActivity) { result ->
                if (result.getOrNull() ?: false) {
                    //连接成功:初始化工具类，跳转设备页
                    val device = deviceList[holder.adapterPosition]
                    val type = BluetoothKit.getDeviceType(device)
                    DeviceKit.onConnected(type)?.let { TagKit.onConnected(it) }
                    startActivity<DeviceActivity>(this@MainActivity) {
                        this.putExtra(DeviceActivity.EXT_DEVICE, device)
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.bluetooth_connect_failure,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                holder.deviceProgress.visibility = View.INVISIBLE
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
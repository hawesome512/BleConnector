package com.hawesome.bleconnector.view.device

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.clj.fastble.data.BleDevice
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.showToast
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.BluetoothKit
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.OnBleDisConnectListener
import kotlinx.android.synthetic.main.activity_nav.*
import java.util.*

/*
* 页面由ViewPager2+BottomNavigationView组成，基于设备模板生成界面
* */
class DeviceActivity : AppCompatActivity(), OnBleDisConnectListener {

    companion object {
        const val TAG = "DeviceActivity"
        const val EXT_DEVICE = "device"
    }

    private var bleDevice: BleDevice? = null
    private val menuItems =
        DeviceKit.getPageMenus()//listOf("realtime","parameter","control","log","ota")
    private val viewModel by lazy {
        ViewModelProvider(this).get(DeviceiewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        BluetoothKit.onDisconnectListener = this
        bleDevice = intent.getParcelableExtra<BleDevice>(EXT_DEVICE)
        title = bleDevice?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewPager.adapter = PageAdapter(this)
        navView.setOnNavigationItemSelectedListener { menuItem ->
            Boolean
            val index = menuItems.map { it.toResString() }.indexOf(menuItem.title.toString())
            viewPager.setCurrentItem(index, true)
            return@setOnNavigationItemSelectedListener true
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                navView.menu[position].setChecked(true)
            }
        })
        viewModel.connectResult.observe(this) {
            if (it.getOrDefault(false)) return@observe
            //重连失败，退出or重试
            AlertDialog.Builder(this).apply {
                setTitle(R.string.bluetooth_disconnect)
                setPositiveButton(R.string.retry) { dialog, which ->
                    viewModel.connect(bleDevice!!)
                }
                setNegativeButton(R.string.exit) { dialog, which ->
                    disposeBle()
                    finish()
                }
            }.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                disposeBle()
                finish()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        disposeBle()
        super.onDestroy()
    }

    private fun disposeBle() {
        BluetoothKit.onDisconnectListener = null
        bleDevice?.let { BluetoothKit.disConnect(it) }
        bleDevice = null
    }

    inner class PageAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount() = menuItems.size

        override fun createFragment(position: Int) =
            DevicePageFragment.newInstance(menuItems[position])

    }

    override fun onDisConnect() {
        R.string.bluetooth_reconnect.showToast()
        viewModel.connect(bleDevice!!)
    }
}
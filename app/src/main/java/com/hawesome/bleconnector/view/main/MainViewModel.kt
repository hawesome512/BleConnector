package com.hawesome.bleconnector.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.clj.fastble.data.BleDevice
import com.hawesome.bleconnector.kit.Repository

class MainViewModel:ViewModel() {

    private val refreshLiveData = MutableLiveData<Any?>()

    private val connectLiveData = MutableLiveData<BleDevice>()

    val bleDeviceList = Transformations.switchMap(refreshLiveData){ Repository.getBleDevices() }

    val connectResult = Transformations.switchMap(connectLiveData){ Repository.connectForResult(it) }

    /*
    *   刷新蓝牙列表
    * */
    fun refresh(){
        //无实际意义，但可以触发switchMap以调用getBleDevices()
        refreshLiveData.value = refreshLiveData.value
    }

    /*
    *   连接蓝牙
    * */
    fun connect(bleDevice: BleDevice){
        connectLiveData.value = bleDevice
    }

}
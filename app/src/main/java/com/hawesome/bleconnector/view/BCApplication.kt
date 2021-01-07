package com.hawesome.bleconnector.view

import android.app.Application
import android.content.Context
import android.util.Log
import com.hawesome.bleconnector.kit.BluetoothKit
import com.uuzuche.lib_zxing.activity.ZXingLibrary

class BCApplication :Application(){

    val TAG = "BCApplication"

    companion object{
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ZXingLibrary.initDisplayOpinion(this)
//        Log.i(TAG, "onCreate: ${BluetoothKit.bleManager.toString()}")
    }
}
package com.hawesome.bleconnector.view

import android.app.Application
import android.content.Context
import android.util.Log
import com.hawesome.bleconnector.kit.BluetoothKit

class BCApplication :Application(){

    val TAG = "BCApplication"

    companion object{
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
//        Log.i(TAG, "onCreate: ${BluetoothKit.bleManager.toString()}")
    }
}
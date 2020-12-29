package com.hawesome.bleconnector.model

import android.util.Log
import androidx.lifecycle.MutableLiveData

/*
* 监控点
* */
data class Tag(val name: String, val address: Int, var value: Int = 0) {
    companion object {
        const val NULL_VALUE = 0
    }

    val valueLiveData = MutableLiveData<Int>()

    fun updateValue(newValue: Int) {
        valueLiveData.value = newValue
        value = newValue
    }
}

package com.hawesome.bleconnector.model

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import kotlin.math.min

/*
*   记录片区
*   是一组连续的数据（Tag*n）*m，记录数目存储在相关监控点Tag中
* */
data class Log(val address: Int, val size: Int, val count: Int, val type: LogType) {

    val indexLoadedLiveData = MutableLiveData<Int>()

    //记录集合：每条记录的首地址为key
    var logMap: HashMap<Int, List<Int>>

    init {
        logMap = HashMap()
        for (i in 0 until count) {
            logMap[address + size * i] = listOf()
        }
    }

    fun setLog(location: Int, data: List<Int>) {
        logMap[location] = data
        val index = logMap.keys.indexOf(location)
        indexLoadedLiveData.postValue(index)
    }

    /*
    *   获取第N条记录的数据
    * */
    fun getLog(index: Int) = logMap[getLocation(index)]

    /*
    *   第N条记录的起始地址
    * */
    fun getLocation(index: Int) = address + min(index, count) * size

    /*
    * 判定记录是否已经读取过
    * 因读记录操作耗时且不会频繁变更，可以先判断内存中是否有存储数据
    * */
    fun didLoaded() = logMap.values.any { it.size > 0 }
}

/*
* 记录基本信息：
* 记录类型，发生时间，记录信息，附加信息（可展开二级页面）
* */
data class LogItem(
    val type: String,
    val time: String,
    val info: String,
    var extras: List<LogItemExtra>? = null
) {
    companion object {
        const val INFO_SEPERATOR = "/"
    }
}

data class LogItemExtra(val name: String, val value: String? = null):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()
    )

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<LogItemExtra> {
        override fun createFromParcel(parcel: Parcel): LogItemExtra {
            return LogItemExtra(parcel)
        }

        override fun newArray(size: Int): Array<LogItemExtra?> {
            return arrayOfNulls(size)
        }
    }

}

/*
*   记录类型
*   XST-7N:转换记录，报警记录，变位记录
* */
enum class LogType(val location: String) { TRSFLOG("6000"), ALARMLOG("7000"), EVENTLOG("8000") }

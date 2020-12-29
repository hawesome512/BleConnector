package com.hawesome.bleconnector.model

import android.graphics.Color
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResColor
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.view.BCApplication


/*
*   设备状态类型
* */
enum class DeviceStatus() {
    OFFLINE, OFF, ON, NORMAL, ALARM;

    //在ALARM时为异常详情
    var info: String? = null

    override fun toString(): String {
        info?.let {
            val items = it.split(DeviceModel.ITEM_INFO_SEPARATOR)
            var text = ""
            items.forEach { item ->
                if (!item.equals(DeviceStatus.OFF.name, true)
                    && !item.equals(DeviceStatus.ON.name, true)
                ) {
                    text += item.toResString() + " "
                }
            }
            return text.trim()
        }
        return name.toResString()
    }

    fun toColor() = name.toResColor()

    companion object {
        fun build(info: String): DeviceStatus {
            if (info.isEmpty()) {
                return NORMAL
            }
            var status = DeviceStatus.values().firstOrNull { info.toUpperCase() == it.name }
                ?: ALARM
            if (status == ALARM) {
                status.info = info
            }
            return status
        }
    }
}
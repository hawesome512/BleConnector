package com.hawesome.bleconnector.kit

import android.content.Context
import android.view.View
import com.google.gson.Gson
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.model.DeviceDisplay
import com.hawesome.bleconnector.model.DeviceModel
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.DeviceType
import com.hawesome.bleconnector.view.BCApplication
import com.hawesome.bleconnector.view.device.ATSStatusDisplay
import com.hawesome.bleconnector.view.device.BarDisplay
import com.hawesome.bleconnector.view.device.ButtonDisplay
import com.hawesome.bleconnector.view.device.display.*

/*
* 设备模型工具类
* onConnected(type: String): DeviceType?        连接成功，设置设备类型
* getPage(title: String)                        获取设备UI页面
* getPageMenus()                               获取页面菜单
* */
object DeviceKit {

    //设备模型
    val deviceModel by lazy {
        val inputText =
            BCApplication.context.resources.openRawResource(R.raw.device).bufferedReader()
                .use { it.readText() }
        val gson = Gson()
        gson.fromJson(inputText, DeviceModel::class.java)
    }

    //连接ing的设备类型：同一时刻限定一台设备连接
    private var connectedDeviceType: DeviceType? = null

    //连接成功，设置设备类型
    fun onConnected(type: String): DeviceType? {
        connectedDeviceType = deviceModel.types.firstOrNull { it.name == type }
        return connectedDeviceType
    }

    //获取设备UI页面
    fun getPage(title: String?) = connectedDeviceType?.pages?.firstOrNull { it.title == title }

    //获取列表所属的UI页面
    fun getPage(item: DevicePageItem) =
        connectedDeviceType?.pages?.firstOrNull { it.content.contains(item) }

    //获取页面菜单
    fun getPageMenus() = connectedDeviceType?.pages?.map { it.title } ?: listOf()

    //获取显示模型
    fun getDisplay(context: Context, pageItem: DevicePageItem): View {
        val display = when (pageItem.getDisplay()) {
            DeviceDisplay.ATSSTATUS -> ATSStatusDisplay(context, pageItem)
            DeviceDisplay.BAR -> BarDisplay(context, pageItem)
            DeviceDisplay.BUTTON -> ButtonDisplay(context, pageItem)
            DeviceDisplay.ONOFF -> OnoffDisplay(context, pageItem)
            DeviceDisplay.RANGE -> RangeDisplay(context, pageItem)
            DeviceDisplay.TEXT -> TextDisplay(context, pageItem)
            DeviceDisplay.TIME -> TimeDisplay(context, pageItem)
            else -> ListDisplay(context, pageItem)
        }
        return display
    }

    //UI显示时拆分列表项
    fun divideItems(item: DevicePageItem) = when (item.getDisplay()) {
        DeviceDisplay.BAR, DeviceDisplay.TIME -> listOf(item)
        else -> item.tags?.map { tag ->
            //优先使用name方便定制处理，多个tag时只能拆开
            val name = if (item.tags.size == 1) item.name else tag
            item.copy(name = name, tags = listOf(tag))
        } ?: listOf()
    }

    //获取列表项
    fun getPageItem(name: String): DevicePageItem? {
        connectedDeviceType?.pages?.forEach { page ->
            page.content.forEach {
                if (it.name == name) return it
            }
        }
        return null
    }

}
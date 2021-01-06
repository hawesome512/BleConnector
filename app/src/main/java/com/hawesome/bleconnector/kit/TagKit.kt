package com.hawesome.bleconnector.kit

import com.hawesome.bleconnector.model.*
import kotlin.math.min

/*
*   设备监控点工具类：
*   onConnected()       连接设备,导入点信息
*   onDisconnected()    断开连接，清空点信息
*   onSuccess()         处理蓝牙返回数据
*   getTags(:List<String>):List<Tag>                获取相关点集合
*   getPageItem(:String):DevicePageItem?            获取相关点的ui模型
*   requestTagQuery(:List<String>:Boolean)          请求读数据
*   requestLogQueryForCount(:Int:Int:Boolean): Int  请求读记录
*   requestTagUpdate(:List<Tag>)                    请求写数据
* */
object TagKit : BleReceivedCallback {

    //普通监控点
    private val tagList = mutableListOf<Tag>()

    //记录数据
    private val logList = mutableListOf<Log>()

    //当前连接的设备类型，依设计方案，同时刻只允许一台外设接入
    private var deviceType: DeviceType? = null

    fun onConnected(type: DeviceType) {
        deviceType = type
        tagList.clear()
        logList.clear()
        type.address.forEach { zone ->
            val address = zone.location.toInt(16)
            if (zone.log > 0) {
                val size = zone.items.size
                val count = zone.log
                val logType = LogType.values().first { it.location == zone.location }
                logList.add(Log(address, size, count, logType))
            } else {
                zone.items.forEachIndexed { index, item ->
                    tagList.add(Tag(item, address = address + index))
                }
            }
        }
    }

    fun onDisconnected() {
        tagList.clear()
        logList.clear()
        deviceType = null
    }

    /*
    *   返回有效数据
    * */
    override fun onSuccess(result: ReceivedResult) {
        when (result) {
            ReceivedResult.WRITE_SUCCESS -> {
                //修改数据成功，执行读数据指令以更新监控点值
                val cmd = ModbusKit.buildReadCMD(result.cmdInfo.toggle())
                val readCMD = BleCommand(cmd, BleCommand.DEFAULT_REPEAT)
                BluetoothKit.executeCMDList(listOf(readCMD))
            }
            ReceivedResult.READ_SUCCESS -> {
                val location = result.cmdInfo.location
                val data = result.data
                //记录数据
                logList.firstOrNull { it.logMap.containsKey(location) }?.let {
                    it.setLog(location, data)
                    return
                }
                //普通监控点
                data.forEachIndexed { index, value ->
                    tagList.firstOrNull { it.address == location + index }?.updateValue(value)
                }
            }
            ReceivedResult.ERROR -> {
            }
        }
    }

    fun getLog(type: String) = logList.firstOrNull { it.type.name == type.toUpperCase() }

    fun getTags(names: List<String>) = tagList.filter { names.contains(it.name) }

    /*
    *   获取监控点相关的页面模型
    * */
    fun getPageItem(tagName: String): DevicePageItem? {
        deviceType?.pages?.forEach { page ->
            page.content.firstOrNull { it.tags?.contains(tagName) ?: false }?.let { return it }
        }
        return null
    }

    /*
    *   请求刷新普通数据片区
    *   实时循环刷新的不需要：失败重试
    * */
    fun requestPageQuery(address: List<String>?, refresh: List<String>?) {
        val bleCommands = mutableListOf<BleCommand>()
        address?.let { bleCommands.addAll(buildCommands(it, BleCommand.DEFAULT_REPEAT)) }
        refresh?.let { bleCommands.addAll(buildCommands(it, BleCommand.ALWAYS_REPEAT)) }
        BluetoothKit.executeCMDList(bleCommands)
    }

    private fun buildCommands(locations: List<String>, retry: Int): List<BleCommand> {
        val bleCommands = mutableListOf<BleCommand>()
        locations.forEach { location ->
            val zone = deviceType!!.address.firstOrNull { it.location == location }
            zone?.let {
                val cmdInfo = CMDInfo(CMDType.READ, it.getLocation(), it.items.size)
                val command = BleCommand(ModbusKit.buildReadCMD(cmdInfo), retry)
                bleCommands.add(command)
            }
        }
        return bleCommands
    }

    /*
    *   请求刷新记录片区数据
    *   记录条数有相应的存储变量点，返回记录片区实际记录条数
    * */
    fun requestLogQueryForCount(
        location: Int,
        recorderCount: Int,
        forceUpdate: Boolean = false
    ): Int {
        logList.firstOrNull { it.address == location }?.let { log ->
            val count = min(recorderCount, log.count)
            //强制刷新or记录未读取则请求数据
            if (forceUpdate || !log.didLoaded()) {
                val bleCommands = mutableListOf<BleCommand>()
                for (i in 0 until count) {
                    val cmdInfo = CMDInfo(CMDType.READ, log.getLocation(i), log.size)
                    bleCommands.add(
                        BleCommand(
                            ModbusKit.buildReadCMD(cmdInfo),
                            BleCommand.DEFAULT_REPEAT
                        )
                    )
                }
                BluetoothKit.executeCMDList(bleCommands)
            }
            return count
        }
        return 0
    }

    /*
    * 请求修改数据
    * */
    fun requestTagUpdate(tags: List<Tag>) {
        tags.firstOrNull()?.let { firstTag ->
            val data = tags.map { it.value }
            val cmdInfo = CMDInfo(CMDType.WRITE, firstTag.address, tags.size)
            val command =
                BleCommand(ModbusKit.buildWriteCMD(cmdInfo, data), BleCommand.DEFAULT_REPEAT)
            BluetoothKit.executeCMDList(listOf(command))
        }
    }

}
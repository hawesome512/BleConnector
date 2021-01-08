package com.hawesome.bleconnector.kit

import com.hawesome.bleconnector.ext.pow
import com.hawesome.bleconnector.ext.toDateTimeFormatter
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.ext.trim
import com.hawesome.bleconnector.model.*
import java.time.LocalDateTime
import java.util.*
import kotlin.math.max

/*
*   点值转换工具类
*   getShowString(tagValue: Int, pageItem: DevicePageItem): String                        获取显示值
*   setUpdateValue(showValue: String, pageItem: DevicePageItem): Int                      获取设定值
*   getUnitFactor(unit: String): Double                                                 获取单位系数
*   getBitStatus(value: Int, items: List<String>?): DeviceStatus                      获取开关位状态
*   getFixedText(value: Int, items: List<String>?): String                                获取固定值
*   getSegmentText(value: Int, items: List<String>?): String                              获取多段值
*   setSegment(tagValue: Int, selectedIndex: Int, items: List<String>?): Int?             设置多段值
*   getVersionText(value: Int): String                                                获取软件版本号
*   getATSModeText(value: Int)：String                                               获取ATS工作模式
*   checkBitIsOne(value: Int, items: List<String>?): Boolean                              判断开关位
*   setBit(tagValue: Int, bitIsOne: Boolean, items: List<String>?): Int?                  设置开关位
*   getBCDTime(value: List<Int>): String                                               获取BCD码时间
*   setBCDTime(dateTime: LocalDateTime): List<Int>                                     设置BCD码时间
* */
object TagValueConverter {

    const val BCD_DATETIME_PATTERN = "yyMMddHHmmss"
    const val SHOW_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

    /*
    *   获取点的显示值
    * */
    fun getShowString(tagValue: Int, pageItem: DevicePageItem): String {
        var value = tagValue.toFloat().toBigDecimal()
        //系数
        pageItem.unit?.toFloatOrNull()?.let {
            value *= it.toBigDecimal()
        }
        val display = DeviceDisplay.valueOf(pageItem.display.toUpperCase())
        return when (display) {
            DeviceDisplay.BIT -> getBitStatus(value.toInt(), pageItem.items).toString()
            DeviceDisplay.LIST,
            DeviceDisplay.ITEM -> getFixedText(value.toInt(), pageItem.items)
            DeviceDisplay.VERSION -> getVersionText(value.toInt())
            DeviceDisplay.SEGMENT -> getSegmentText(value.toInt(), pageItem.items)
            DeviceDisplay.ATSMODE -> getATSModeText(value.toInt())
            else -> value.toFloat().trim()
        }
    }

    /*
    * 获取普通设定目标值，需基于单位系数进行逆转换
    * */
    fun setUpdateValue(showValue: String, pageItem: DevicePageItem): Int {
        var sourceValue = (showValue.toDoubleOrNull() ?: Tag.NULL_VALUE.toDouble()).toBigDecimal()
        pageItem.unit?.let { unit ->
            sourceValue /= getUnitFactor(unit).toBigDecimal()
        }
        return sourceValue.toInt()
    }

    /*
    * 单位换算
    * unit:1.0,1.0*In,1.0*In*Ir……其中In,Ir需要调用getString进行换算
    * */
    fun getUnitFactor(unit: String): Double {
        var factor = 1.0.toBigDecimal()
        val infos = unit.split(DeviceModel.UNIT_INFO_SEPARATOR)
        for (i in 0 until infos.size) {
            val doubleFactor = infos[i].toDoubleOrNull()
            if (doubleFactor != null) {
                factor *= doubleFactor.toBigDecimal()
                continue
            }
            val unitTagValue = TagKit.getTags(listOf(infos[i])).firstOrNull()?.value
            val pageItem = TagKit.getPageItem(infos[i])
            if (unitTagValue != null && pageItem != null) {
                val realUnitValue = getShowString(unitTagValue, pageItem)
                factor *= (realUnitValue.toDoubleOrNull() ?: 1.0).toBigDecimal()
            }
        }
        return factor.toDouble()
    }


    /*
    * 开关位转换
    * items:["overload","","off/on"]
    * */
    fun getBitStatus(value: Int, items: List<String>?): DeviceStatus {
        var infoString = ""
        items?.let { items ->
            if (value < 0) {
                return@let
            }
            items.forEachIndexed { index, item ->
                if (item != DeviceModel.ITEM_NULL) {
                    val flag = 2.pow(index)
                    // 0 or 1
                    val indexValue = (value and flag) / flag
                    //某些开关位0/1分别表示不同意义,e.g.:OPEN/CLOSE;否则，就在bit=1时才有状态值
                    val itemInfos = item.split(DeviceModel.ITEM_INFO_SEPARATOR)
                    if (itemInfos.size == 2) {
                        infoString += itemInfos[indexValue] + DeviceModel.ITEM_INFO_SEPARATOR
                    } else if (indexValue == 1) {
                        infoString += itemInfos[0] + DeviceModel.ITEM_INFO_SEPARATOR
                    }
                }
            }
        }
        infoString = infoString.removeSuffix(DeviceModel.ITEM_INFO_SEPARATOR)
        return DeviceStatus.build(infoString)
    }

    /*
    * 固定值转换
    * items:["0/zero","1/one"]
    * */
    fun getFixedText(value: Int, items: List<String>?): String {
        if (items.isNullOrEmpty()) return value.toString()
        val seperator = DeviceModel.ITEM_INFO_SEPARATOR
        // "0/"
        var index = items.indexOfFirst { it.contains("$value$seperator") }
        index = max(0, index)
        return items[index].split(seperator).last()
    }

    /*
    * 软件版本号
    * value:257→0x0101→V1.01
    * */
    fun getVersionText(value: Int) = String.format("V%X.%02X", value / 0x100, value % 0x100)

    /*
    * 获取多段值
    * items:["-1","0","10/local","20/remote"]→使用整个值作判断
    *       ["1","2","0/zero","1/one","2/two","3/three"]
    * */
    fun getSegmentText(value: Int, items: List<String>?): String {
        if (items.isNullOrEmpty() || items.size <= 2) return value.toString()
        val separator = DeviceModel.ITEM_INFO_SEPARATOR
        val location = items[0].toIntOrNull()
        val size = items[1].toIntOrNull()
        if (location == null || size == null) return value.toString()
        val validValue = if (location < 0) value else (value % 2.pow(location + size)) shr location
        val item = items.firstOrNull { it.contains("$validValue$separator") } ?: items[2]
        return item.split(separator).last()
    }

    /*
    * 设置多段值
    * items:["1","2","0/zero","1/one","2/two","3/three"]→使用整个值作判断
    * selectedIndex = 2→"2/two"→bit:x10x
    * */
    fun setSegment(tagValue: Int, selectedIndex: Int, items: List<String>?): Int? {
        if (items.isNullOrEmpty() || items.size <= selectedIndex + 2) return null
        val targetValue = getFirstIntFromItems(items.subList(selectedIndex + 2, selectedIndex + 3))
        val location = items[0].toIntOrNull() ?: -1
        val size = items[1].toIntOrNull() ?: 0
        //不区分开关位
        if (targetValue == null || location < 0) return targetValue
        //tagValue:[0b]1001 0001，left:1001 0000，right:0000 0001
        val middleValue = targetValue shl location
        val leftValue = tagValue / 2.pow(location + size) shl (location + size)
        val rightValue = tagValue % 2.pow(location)
        return leftValue + middleValue + rightValue
    }

    /*
    * ATS工作模式
    * 开关位，根据优先级别来显示
    * 1.消防模式
    * 手动模式：2.手动不并联 or 3.手动并联
    * 自动模式：4.自投自复 or 5.自投不复
    * */
    fun getATSModeText(value: Int): String {
        val mode = if (checkBitIsOne(value, listOf("10"))) "FireMode"
        else if (checkBitIsOne(value, listOf("8"))) {
            if (checkBitIsOne(value, listOf("11"))) "ManualParallel" else "NoneParallel"
        } else {
            if (checkBitIsOne(value, listOf("7"))) "SMmanualBack" else "SMautoBack"
        }
        return mode.toResString()
    }

    /*
    * 判断开关位是否为1
    * items可能包含多余信息：items[0]="0/off/on"
    * */
    fun checkBitIsOne(value: Int, items: List<String>?): Boolean {
        getFirstIntFromItems(items)?.let { bit ->
            val flag = 2.pow(bit)
            return (value and flag) == flag
        }
        return false
    }

    /*
    * 设置开关位：0,1
    * 修改原点值tagValue的开关位
    * items可能包含多余信息：items[0]="0/off/on"
    * */
    fun setBit(tagValue: Int, bitIsOne: Boolean, items: List<String>?): Int? {
        getFirstIntFromItems(items)?.let { bit ->
            return if (bitIsOne) tagValue or 2.pow(bit)
            else tagValue and (2.pow(16) - 1 - 2.pow(bit))
        }
        return null
    }

    /*
    * 将bcd码转为时间文本
    * Hex:[2010][1012][1212]→2020年10月10日12:12:12
    * */
    fun getBCDTime(value: List<Int>): String {
        val outFormatter = SHOW_DATETIME_PATTERN.toDateTimeFormatter()
        if (value.size != 3) return LocalDateTime.now().format(outFormatter)
        val hexValue = String.format("%04X%04X%04X", value[0], value[1], value[2])
        val dateTime = LocalDateTime.parse(hexValue, BCD_DATETIME_PATTERN.toDateTimeFormatter())
        return dateTime.format(outFormatter)
    }

    /*
    * 将日期时间转为bcd码数组
    * */
    fun setBCDTime(dateTime: LocalDateTime): List<Int> {
        val hexValue = dateTime.format(BCD_DATETIME_PATTERN.toDateTimeFormatter())
        val data = mutableListOf<Int>()
        for (i in 0..2) {
            data.add(hexValue.substring(i * 4, (i + 1) * 4).toInt(16))
        }
        return data
    }

    /*
    * 判定本地、远程模式
    * authority:CtrlMode/9/0/local/1/remote
    * */
    fun checkLocalLocked(value: Int, authority: String): Boolean {
        val infos = authority.split(DeviceModel.ITEM_INFO_SEPARATOR)
        val bitValue = if (infos[1].toInt() > 0) {
            if (checkBitIsOne(value, listOf(infos[1]))) 1 else 0
        } else {
            value
        }
        val index = infos.indexOfFirst { it == bitValue.toString() }
        return infos[index + 1] == DeviceModel.LOCAL
    }

    /*
    * 获取items中的首项整数：items[0]="0/off/on"
    * */
    private fun getFirstIntFromItems(items: List<String>?): Int? {
        if (items.isNullOrEmpty()) return null
        return items[0].split(DeviceModel.ITEM_INFO_SEPARATOR).firstOrNull()?.toIntOrNull()
    }
}
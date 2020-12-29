package com.hawesome.bleconnector.kit

import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.model.Log
import com.hawesome.bleconnector.model.LogItem
import com.hawesome.bleconnector.model.LogItemExtra
import com.hawesome.bleconnector.model.LogType

/*
* 处理记录信息的工具类
* */
object LogKit {

    fun getLogData(log: Log, index: Int): LogItem? {
        log.getLog(index)?.let {
            return when (log.type) {
                LogType.TRSF -> cvtTrsfInfo(it)
                LogType.EVENT -> cvtEventInfo(it)
                LogType.ALARM -> cvtAlarmInfo(it)
            }
        }
        return null
    }

    /*
    *   故障切换记录
    * */
    private fun cvtTrsfInfo(values: List<Int>): LogItem {
        //[timeH,timeM,timeL,trsfInfo,errorInfo]
        val subData = listOf(values[0], values[1], values[2], values[4], values[5])

        val time = TagValueConverter.getBCDTime(subData.subList(0, 3))

        //投切位置：101/110
        val startLocation = cvtATSTrsfLocation(subData[3] % 0x100)
        val endLocation = cvtATSTrsfLocation(subData[3] / 0x100)
        val info = startLocation + LogItem.INFO_SEPERATOR + endLocation

        val type1 = when (subData[4] / 0x100) {
            0x01 -> "LP1"
            0x02 -> "LP2"
            else -> "unknown"
        }.toResString()
        val type2Items = listOf(
            "openPhase",
            "noVoltage",
            "underVoltage",
            "overVoltage",
            "underFrequency",
            "overFrequency",
            "unblance",
            "reversePhase"
        )
        val type2 = TagValueConverter.getBitStatus(subData[4] % 0x100, type2Items).toString()
        val type = "$type1($type2)"
        val logItem = LogItem(type, time, info)
        logItem.extras = cvtTrsfExtra(logItem, values)
        return logItem
    }

    /*
    * 变位记录
    * values:[timeH,timeM,timeL,typeInfo,startInfo,endInfo]
    * */
    private fun cvtEventInfo(values: List<Int>): LogItem {
        val time = TagValueConverter.getBCDTime(values.subList(0, 3))
        val type = when (values[3]) {
            0x01 -> "localManual"
            0x02 -> "parallel"
            0x04 -> "remoteManual"
            0x11 -> "autobackAuto"
            0x12 -> "manualbackAuto"
            0x14 -> "manualAction"
            0x16 -> "errorTrip"
            0x20 -> "Fire"
            else -> "unknown"
        }.toResString()
        val startLocation = cvtATSTrsfLocation(values[4])
        val endLocation = cvtATSTrsfLocation(values[5])
        val info = startLocation + LogItem.INFO_SEPERATOR + endLocation
        return LogItem(type, time, info)
    }

    /*
    * 报警记录
    * values:[timeH,timeM,timeL,alarmInfo]
    * */
    private fun cvtAlarmInfo(values: List<Int>): LogItem {
        val time = TagValueConverter.getBCDTime(values.subList(0, 3))
        val type = when (values[3]) {
            0x01 -> "generator"
            0x02 -> "allPower"
            0x04 -> "action"
            0x08 -> "trip"
            0x10 -> "parallel"
            else -> "unknown"
        }.toResString()
        return LogItem(type, time, "")
    }

    /*
    * ATS转换记录详情（二级页面中显示）
    * */
    private fun cvtTrsfExtra(logItem: LogItem, values: List<Int>) =
        mutableListOf<LogItemExtra>().apply {
            //数据块一：基本信息，null用于列表中区分数据块
            add(LogItemExtra("LogDetail".toResString()))
            add(LogItemExtra("LogTime".toResString(), logItem.time))
            add(LogItemExtra("LogType".toResString(), logItem.type))
            add(LogItemExtra("LogState".toResString(), logItem.info.replace("/", " → ")))
            add(LogItemExtra("LogThreshold".toResString(), cvtThreshold(values[5], values[6])))

            val voltageItems = if (values[3] == 0x03) listOf("Ua(V)", "Ub(V)", "Uc(V)") else listOf(
                "Uab(V)",
                "Ubc(V)",
                "Uca(V)"
            )
            //数据块二：I电
            add(LogItemExtra("Source1".toResString()))
            add(LogItemExtra(voltageItems[0], values[7].toString()))
            add(LogItemExtra(voltageItems[1], values[8].toString()))
            add(LogItemExtra(voltageItems[2], values[9].toString()))
            add(LogItemExtra("F(Hz)", (values[10].toDouble() / 100).toString()))
            add(LogItemExtra("P.S.", cvtPhase(values[11])))
            add(LogItemExtra("Uun(%)", (values[12].toDouble() / 10).toString()))
            //数据块二：II电
            add(LogItemExtra("Source2".toResString()))
            add(LogItemExtra(voltageItems[0], values[13].toString()))
            add(LogItemExtra(voltageItems[1], values[14].toString()))
            add(LogItemExtra(voltageItems[2], values[15].toString()))
            add(LogItemExtra("F(Hz)", (values[16].toDouble() / 100).toString()))
            add(LogItemExtra("P.S.", cvtPhase(values[17])))
            add(LogItemExtra("Uun(%)", (values[18].toDouble() / 10).toString()))
        }

    /*
    * 相序转换
    * */
    private fun cvtPhase(value: Int) = when (value) {
        70 -> "PHabc"
        73 -> "PHacb"
        else -> "PHopen"
    }.toResString()

    /*
    * 记录中的阈值依故障类型而取不同的保护整定值单位
    * 类型                        阈值
    * 0x01:断相                 无阈值
    * 0x02:失压                 无阈值
    * 0x04/0x08:欠压、过压         x1V
    * 0x10/0x20:过频、欠频      x100Hz
    * 0x40:不平衡度               x10%
    * 0x80:逆序                 无阈值
    * */
    private fun cvtThreshold(type: Int, value: Int): String {
        val typeL = type % 0x100
        val nullThreshold = "---"
        return when {
            typeL / 0x80 > 0 -> nullThreshold
            typeL / 0x40 > 0 -> "${value.toDouble() / 10}%"
            typeL / 0x10 > 0 -> "${value.toDouble() / 100}Hz"
            typeL / 0x04 > 0 -> "${value}V"
            else -> return nullThreshold
        }
    }

    /*
    * 将ATS投切位置101转换为IOI
    * */
    private fun cvtATSTrsfLocation(value: Int) =
        value.toString(2).padStart(3, '0').replace('0', 'O').replace('1', 'I')
}
package com.hawesome.bleconnector.kit

import com.hawesome.bleconnector.ext.toIntList

//<editor-fold desc="定义数据类型">
/*
*   Modbus指令类型：读、写、错误
* */
enum class CMDType(val value: Int) {
    READ(0x03), WRITE(0x10), ERROR(0x00)
}

/*
*   Modbus指令信息(指令类型，起始地址，数据长度）
* */
data class CMDInfo(val type: CMDType = CMDType.ERROR, val location: Int = 0, val size: Int = 0) {

    /*
    *   修改参数后马上需要验证其结果
    * */
    fun toggle(): CMDInfo {
        val toggleType = when (type) {
            CMDType.WRITE -> CMDType.READ
            CMDType.READ -> CMDType.WRITE
            CMDType.ERROR -> CMDType.ERROR
        }
        return CMDInfo(toggleType, location, size)
    }
}

/*
*   Modbus返回数据结果：错误，写成功，读成功（有效数据）
* */
enum class ReceivedResult(var data: List<Int> = listOf(), var cmdInfo: CMDInfo = CMDInfo()) {
    ERROR, WRITE_SUCCESS, READ_SUCCESS
}
//</editor-fold>

/*
*   Modbus通信处理的工具类
*   buildReadCMD(:CMDInfo):ByteArray  生成读指令
*   buildWriteCMD(:CMDInfo:List<Int>):ByteArray   生成写指令
*   varifyReceivedData(:ByteArray:ByteArray):ReceivedResult 验证返回数据
* */
object ModbusKit {

    //跟外设通信，统一采用Modbus通信地址：1，设备不检验通信地址与本机是否匹配
    private const val FIXED_ADDRESS = 0x01

    /*
    *   Modbus读指令，location:起始地址，size:数据长度
    * */
    fun buildReadCMD(info: CMDInfo): ByteArray {
        val tmps = mutableListOf(FIXED_ADDRESS, CMDType.READ.value)
        tmps.addAll(seperateInt(info.location))
        tmps.addAll(seperateInt(info.size))
        tmps.addAll(buildCRC(tmps))
        return ByteArray(tmps.size) { tmps[it].toByte() }
    }

    /*
    *   Modbus写指令，location:起始地址，values:写数据，一个电量参数占一位
    * */
    fun buildWriteCMD(info: CMDInfo, values: List<Int>): ByteArray {
        val temps = mutableListOf(FIXED_ADDRESS, CMDType.WRITE.value)
        temps.addAll(seperateInt(info.location))
        temps.addAll(seperateInt(info.size))
        temps.add(values.size * 2)
        values.forEach { temps.addAll(seperateInt(it)) }
        temps.addAll(buildCRC(temps))
        return ByteArray(temps.size) { temps[it].toByte() }
    }

    /*
    *   验证返回数据，判定通讯是否成功
    * Read[HEX]:
    *   snd→01,03,P1,P2,00,N,CRC1,CRC2【size=8】
    *   rcv→01,03,N*2,X0,X1……X2n-1,CRC1,CRC2【size≥7】
    * Write[HEX]:
    *   snd→01,10,P1,P2,00,N,2N,X0,X1……X2n-1,CRC1,CRC2【size≥11】
    *   rcv→01,10,P1,P2,00,N,CRC1,CRC2【size=8】
    * */
    fun varifyReceivedData(send: ByteArray, received: ByteArray): ReceivedResult {
        val snd = send.toIntList()
        val rcv = received.toIntList()
        val cmdInfo = extractCMDInfo(send)
        //读写功能指令（含crc)最小长度，防止数组长度溢出，功能码一致
        if (snd.size >= 8 && rcv.size >= 7 && snd[1] == rcv[1]) {
            if (snd[1] == CMDType.WRITE.value && snd.subList(2, 3) == rcv.subList(2, 3)) {
                val result = ReceivedResult.WRITE_SUCCESS
                result.cmdInfo = cmdInfo
                return result
            }
            if (snd[1] == CMDType.READ.value && mergeInt(snd[4], snd[5]) * 2 == rcv[2]) {
                val data = mutableListOf<Int>()
                for (i in 0 until rcv[2] / 2) {
                    data.add(mergeInt(rcv[2 * i + 3], rcv[2 * i + 4]))
                }
                val result = ReceivedResult.READ_SUCCESS
                result.data = data
                result.cmdInfo = cmdInfo
                return result
            }
        }
        return ReceivedResult.ERROR
    }

    /*
    *  验证指令是否完整
    * */
    fun checkCMDFull(cmd: ByteArray): Boolean {
        val cmds = cmd.toIntList()
        val sourceCRC = cmds.subList(cmds.size - 2, cmds.size)
        val targetCRC = buildCRC(cmds.subList(0, cmds.size - 2))
        return sourceCRC == targetCRC
    }

    /*
    * 提取基本指令信息（读写通用）
    * */
    private fun extractCMDInfo(cmd: ByteArray): CMDInfo {
        val temps = cmd.toIntList()
        if (temps.size < 6) return CMDInfo()
        val type = enumValues<CMDType>().firstOrNull { it.value == temps[1] }
        val location = mergeInt(temps[2], temps[3])
        val size = mergeInt(temps[4], temps[5])
        return CMDInfo(type ?: CMDType.ERROR, location, size)
    }

    /*
    *   整型数据分解为高低位数组：65535→【255,255】
    * */
    private fun seperateInt(source: Int): List<Int> {
        val tmps = mutableListOf(0xFF, 0XFF)
        tmps[0] = source / 0x100
        tmps[1] = source % 0x100
        return tmps
    }

    /*
    * 将高低位数据合并为整型数据：【255,255】→65535
    * */
    private fun mergeInt(vararg sources: Int) =
        if (sources.size != 2) 0 else (sources[0] * 0x100 + sources[1])

    /*
    *   生成CRC校验码
    *   HEX:【01,03,00,00,00,01】→【84,0A】
    * */
    private fun buildCRC(sources: List<Int>): List<Int> {
        var temp = 0x0000FFFF
        val polynomial = 0x0000A001
        sources.forEach { source ->
            temp = temp xor (source and 0x000000FF)
            (0 until 8).forEach {
                if (temp and 0x00000001 != 0) {
                    temp = temp shr 1
                    temp = temp xor polynomial
                } else {
                    temp = temp shr 1
                }
            }
        }
        //高低位互换，符合士电CRC校验格式
        return seperateInt(temp).reversed()
    }
}
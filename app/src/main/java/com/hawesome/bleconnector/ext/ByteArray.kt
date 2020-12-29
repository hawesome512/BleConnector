package com.hawesome.bleconnector.ext

fun ByteArray.toIntList() = this.map { it.toUByte().toInt() }

fun ByteArray.toList() = this.toIntList()

operator fun ByteArray.plus(item: ByteArray): ByteArray {
    val temps = this.toIntList() + item.toIntList()
    return ByteArray(temps.size) { temps[it].toByte() }
}
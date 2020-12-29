package com.hawesome.bleconnector.ext

import java.math.BigDecimal

fun Float.trim() = this.toString().trimEnd('0').trim('.')

fun Float.toBigDecimal() = BigDecimal.valueOf(this.toDouble())
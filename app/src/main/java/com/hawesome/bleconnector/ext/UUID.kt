package com.hawesome.bleconnector.ext

import java.util.*

infix fun UUID.has(key: String) = this.toString().contains(key,true)
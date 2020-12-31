package com.hawesome.bleconnector.view

import com.hawesome.bleconnector.model.Tag
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt
import kotlin.random.Random

enum class Person(val text: String) { TEACHER("t"), STUDENT("s"), WORKER("w") }

fun main() {
    println(4.1.roundToInt())
    println(4.5.roundToInt())
}

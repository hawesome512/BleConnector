package com.hawesome.bleconnector.ext

import android.widget.Toast
import com.hawesome.bleconnector.view.BCApplication

/*
* 简化Toast用法
* */

fun String.showToast() {
    val context = BCApplication.context
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun Int.showToast() {
    val context = BCApplication.context
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun CharSequence.showToast() {
    this.toString().showToast()
}
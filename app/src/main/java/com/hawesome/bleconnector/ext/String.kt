package com.hawesome.bleconnector.ext

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.view.BCApplication
import java.time.format.DateTimeFormatter

/*
* 转换为资源颜色
* */
fun String.toResColor(): Int {
    val context = BCApplication.context
    val id =
        context.resources.getIdentifier(this.toValidResourceName(), "color", context.packageName)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        context.getColor(id)
    } else {
        R.color.black
    }
}

/*
* 转化为资源图片
* */
fun String.toResDrawable(): Drawable? {
    val context = BCApplication.context
    val id =
        context.resources.getIdentifier(this.toValidResourceName(), "drawable", context.packageName)
    return context.getDrawable(id)
}

/*
* 转换为资源文本
* */
fun String.toResString(): String {
    val context = BCApplication.context
    val id =
        context.resources.getIdentifier(this.toValidResourceName(), "string", context.packageName)
    try {
        return context.getString(id)
    } catch (exption: Exception) {
        return this
    }
}

fun String.toUnitString(): SpannableString {
    val spannable = SpannableString(this)
    val context = BCApplication.context
    val nameColor = context.getColor(R.color.blue_500)
    val unitColor = context.getColor(R.color.secondary_text)
    val index = this.indexOf('(')
    if (index >= 0) {
        spannable.setSpan(
            ForegroundColorSpan(nameColor),
            0,
            index,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(unitColor),
            index,
            this.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}

/*
* 转换为日期时间格式
* */
fun String.toDateTimeFormatter() = DateTimeFormatter.ofPattern(this)

/*
* 资源文件命名只能包含：数字、小写字母和下划线
* */
fun String.toValidResourceName(): String {
    val regex = Regex("[^_a-z0-9]")
    return this.toLowerCase().replace(regex, "")
}
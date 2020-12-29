package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toDateTimeFormatter
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.OnTagListener
import kotlinx.android.synthetic.main.display_time.view.*
import java.time.LocalDateTime

class TimeDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener {

    init {
        inflate(context, R.layout.display_time, this)
        observeTagUpdate(context, pageItem, ::updateViews)
    }

    private fun updateViews(tags: List<Tag>) {
        deviceTime.text = TagValueConverter.getBCDTime(tags.map { it.value })
        localTime.text = LocalDateTime.now()
            .format(TagValueConverter.SHOW_DATETIME_PATTERN.toDateTimeFormatter())
    }
}
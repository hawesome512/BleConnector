package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Toast
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toDateTimeFormatter
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.ConfirmActivity
import com.hawesome.bleconnector.view.device.OnModifyListener
import com.hawesome.bleconnector.view.device.OnTagListener
import kotlinx.android.synthetic.main.display_list.view.*
import kotlinx.android.synthetic.main.display_time.view.*
import kotlinx.android.synthetic.main.display_time.view.nameText
import java.time.LocalDateTime

class TimeDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener, OnModifyListener {

    lateinit var tagList: ArrayList<Tag>

    init {
        inflate(context, R.layout.display_time, this)
        observeTagUpdate(context, pageItem) { tags ->
            tagList = tags as ArrayList<Tag>
            deviceTime.text = TagValueConverter.getBCDTime(tags.map { it.value })
            localTime.text = LocalDateTime.now()
                .format(TagValueConverter.SHOW_DATETIME_PATTERN.toDateTimeFormatter())

        }
        setOnClickListener {
            Toast.makeText(
                context,
                "${nameText.text}",
                Toast.LENGTH_SHORT
            ).show()
        }
        updateImage.setOnClickListener {
            val values = TagValueConverter.setBCDTime(LocalDateTime.now())
            val tags = tagList.mapIndexed { index, tag -> tag.copy(value = values[index]) }
            startActivity<ConfirmActivity>(context) {
                val title = "${nameText.text}:${localTime.text}"
                putExtra(ConfirmActivity.EXT_TITLE, title)
                putParcelableArrayListExtra(ConfirmActivity.EXT_TAGS, tagList)
            }
        }
    }
}
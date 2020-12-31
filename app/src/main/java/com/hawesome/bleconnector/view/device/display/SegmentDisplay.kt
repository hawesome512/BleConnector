package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.ext.toUnitString
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DeviceModel
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.view.device.OnTagListener
import kotlinx.android.synthetic.main.display_list.view.nameText
import kotlinx.android.synthetic.main.display_segment.view.*

class SegmentDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener {

    init {
        inflate(context, R.layout.display_segment, this)

        //items:["-1","0","10/A","20/B"]
        val items = pageItem.items ?: listOf()
        val layoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f / items.size)
        layoutParams.setMargins(1, 0, 1, 0)
        val radioList = mutableListOf<RadioButton>()
        for (index in 2 until items.size) {
            val infos = items[index].split(DeviceModel.ITEM_INFO_SEPARATOR)
            val radioButton = RadioButton(context, null, 0, R.style.RadioButton)
            radioButton.isClickable = true
            radioButton.setBackgroundResource(R.drawable.bg_radio_button)
            radioButton.textSize = context.resources.getDimension(R.dimen.font_body)
            radioButton.text = infos.last().toResString()
            radioButton.layoutParams = layoutParams
            segment.addView(radioButton)
            radioList.add(radioButton)
        }
        observeTagUpdate(context, pageItem) {
            val textValue = TagValueConverter.getSegmentText(it[0].value, items).toResString()
            radioList.firstOrNull { it.text == textValue }?.isChecked = true
        }
    }
}
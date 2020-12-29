package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.OnTagListener
import kotlinx.android.synthetic.main.display_list.view.*

class ListDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener {

    init {
        inflate(context, R.layout.display_list, this)
        nameText.text = pageItem.name.toResString()
        observeTagUpdate(context, pageItem, ::updateViews)
    }

    private fun updateViews(tags: List<Tag>) {
        valueText.text = TagValueConverter.getShowString(tags[0].value, pageItem).toResString()
    }

}
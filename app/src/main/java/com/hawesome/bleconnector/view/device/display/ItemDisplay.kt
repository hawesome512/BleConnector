package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DeviceModel
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.view.device.OnModifyListener
import com.hawesome.bleconnector.view.device.OnTagListener
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.display_slider.view.*
import kotlin.math.roundToInt

class ItemDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener, OnModifyListener {

    init {
        inflate(context, R.layout.display_slider, this)
        initViews()
    }

    private fun initViews() {
        //"items": ["0/OOO","1/OOI","3/OII","4/IOO","5/IOI","6/IIO","256/Fire"]
        val items = pageItem.items
        if (items.isNullOrEmpty()) return
        seekBar.setIndicatorTextFormat("\${TICK_TEXT}")
        val showItems = items.map { it.split(DeviceModel.ITEM_INFO_SEPARATOR).last().toResString() }
        seekBar.min = 0f
        seekBar.max = (items.size - 1).toFloat()
        seekBar.tickCount = showItems.size
        seekBar.customTickTexts(showItems.toTypedArray())
        observeTagUpdate(context, pageItem) {
            val tagValue = it[0].value
            val showValue = TagValueConverter.getShowString(tagValue, pageItem).toResString()
            val progress = showItems.indexOf(showValue).toFloat()
            seekBar.setProgress(progress)
        }

    }

    override fun onModifyRequest(): Int? {
        val index = seekBar.progress
        return pageItem.items?.get(index)?.split(DeviceModel.ITEM_INFO_SEPARATOR)?.first()
            ?.toIntOrNull()
    }
}
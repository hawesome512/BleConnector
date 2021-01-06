package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.hawesome.bleconnector.R
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

class SliderDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener, OnSeekChangeListener, OnModifyListener {

    private var min = 0f
    private var max = 100f
    private var step = 1f

    init {
        inflate(context, R.layout.display_slider, this)
        initViews()
    }

    private fun initViews() {
        //items=["*","0","0.1","10"]
        val items = pageItem.items
        if (items.isNullOrEmpty() || items[0] != DeviceModel.ITEM_STEP_ARRAY) return
        min = items[1].toFloat()
        step = items[2].toFloat()
        max = items[3].toFloat()
        seekBar.min = min
        seekBar.max = max
        seekBar.onSeekChangeListener = this
        observeTagUpdate(context, pageItem) {
            val tagValue = it[0].value
            val progress = TagValueConverter.getShowString(tagValue, pageItem).toFloat()
            seekBar.setProgress(progress)
        }

    }

    override fun onSeeking(seekParams: SeekParams?) {
        val index = (seekBar.progressFloat - min) / step
        seekBar.setProgress(index.roundToInt() * step + min)
    }

    override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
    }

    override fun onModifyRequest(): Int? {
        val showValue = seekBar.progressFloat.toString()
        return TagValueConverter.setUpdateValue(showValue,pageItem)
    }
}
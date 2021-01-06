package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.ext.toUnitString
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DeviceDisplay
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.OnTagListener
import com.hawesome.bleconnector.view.device.ParamActivity
import com.hawesome.bleconnector.view.device.log.LogActivity
import kotlinx.android.synthetic.main.display_list.view.*

class ListDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener {

    init {
        inflate(context, R.layout.display_list, this)
        nameText.text = pageItem.name.toResString().toUnitString()

        setOnClickListener {
            when (pageItem.getDisplay()) {
                DeviceDisplay.ITEM, DeviceDisplay.SEGMENT, DeviceDisplay.SLIDER -> {
                    startActivity<ParamActivity>(context) {
                        putExtra(ParamActivity.EXT_ITEM, pageItem.name)
                    }
                }
                DeviceDisplay.LOG -> {
                    val count = valueText.text.toString().toIntOrNull() ?: 0
                    if (count > 0) {
                        startActivity<LogActivity>(context) {
                            putExtra(LogActivity.EXT_NAME, pageItem.name)
                            putExtra(LogActivity.EXT_COUNT, count)
                        }
                    } else {
                        showToast()
                    }
                }
                else -> {
                    showToast()
                }
            }
        }

        observeTagUpdate(context, pageItem, ::updateViews)
    }

    private fun showToast() {
        Toast.makeText(
            context,
            "${nameText.text}(${valueText.text})",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateViews(tags: List<Tag>) {
        valueText.text = TagValueConverter.getShowString(tags[0].value, pageItem).toResString()
    }

}
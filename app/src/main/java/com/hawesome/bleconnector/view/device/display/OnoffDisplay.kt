package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Toast
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.showToast
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DeviceDisplay
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.ConfirmActivity
import com.hawesome.bleconnector.view.device.OnModifyListener
import com.hawesome.bleconnector.view.device.OnTagListener
import com.hawesome.bleconnector.view.device.ParamActivity
import kotlinx.android.synthetic.main.display_list.view.*
import kotlinx.android.synthetic.main.display_list.view.nameText
import kotlinx.android.synthetic.main.display_onoff.view.*

class OnoffDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), OnTagListener, OnModifyListener {

    lateinit var onoffTag: Tag

    init {
        inflate(context, R.layout.display_onoff, this)
        nameText.text = pageItem.name.toResString()
        observeTagUpdate(context, pageItem) {
            onoffTag = it[0]
            funcSwitch.isChecked = TagValueConverter.checkBitIsOne(onoffTag.value, pageItem.items)
        }

        setOnClickListener {
            nameText.text.showToast()
        }
        funcSwitch.setOnClickListener {
            val isOne = funcSwitch.isChecked
            val newValue = TagValueConverter.setBit(onoffTag.value, isOne, pageItem.items)
            if (newValue == null) return@setOnClickListener
            val tag = onoffTag.copy(value = newValue)
            startActivity<ConfirmActivity>(context) {
                val stateID = if (isOne) R.string.freqswitch_on else R.string.freqswitch_off
                val state = context.getString(stateID)
                val title = "${nameText.text}:$state"
                putExtra(ConfirmActivity.EXT_TITLE, title)
                putParcelableArrayListExtra(ConfirmActivity.EXT_TAGS, arrayListOf(tag))
            }
        }
    }
}
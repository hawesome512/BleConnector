package com.hawesome.bleconnector.view.device

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.DeviceDisplay
import com.hawesome.bleconnector.view.device.display.ItemDisplay
import com.hawesome.bleconnector.view.device.display.SegmentDisplay
import com.hawesome.bleconnector.view.device.display.SliderDisplay
import kotlinx.android.synthetic.main.activity_param.*

class ParamActivity : AppCompatActivity() {

    companion object {
        const val EXT_ITEM = "item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_param)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val itemName = intent.getStringExtra(EXT_ITEM)!!
        DeviceKit.getPageItem(itemName)?.let { pageItem ->
            title = pageItem.name.toResString()
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topToTop = R.id.container
            layoutParams.bottomToBottom = R.id.authorityText
            val child = when (pageItem.getDisplay()) {
                DeviceDisplay.SEGMENT -> SegmentDisplay(this, pageItem)
                DeviceDisplay.SLIDER -> SliderDisplay(this, pageItem)
                DeviceDisplay.ITEM -> ItemDisplay(this, pageItem)
                else -> View(this)
            }
            child.layoutParams = layoutParams
            container.addView(child)

            updateBtn.setOnClickListener {
                val newValue = (child as? OnModifyListener)?.onModifyRequest()
                val tag = TagKit.getTags(pageItem.tags ?: listOf()).firstOrNull()
                if (newValue == null || tag == null) return@setOnClickListener
                val newTag = tag.copy(value = newValue)
                TagKit.requestTagUpdate(listOf(newTag))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.hawesome.bleconnector.view.device

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setMargins
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResColor
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.DeviceModel
import com.hawesome.bleconnector.model.DevicePageItem
import kotlinx.android.synthetic.main.display_button.view.*

class ButtonDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {


    init {
        inflate(context, R.layout.display_button, this)
        //"items": ["IOO/4/blue","OOI/1/blue","OOO/0/blue"]
        val items = pageItem.items ?: listOf<String>()
        val space = context!!.resources.getDimension(R.dimen.medium_space).toInt()
        val textSize = context!!.resources.getDimension(R.dimen.font_headline)
        val textColor = context.getColor(R.color.white)
        val layoutParams = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1.0f / items.size
        )
        val buttonTag = TagKit.getTags(pageItem.tags!!)[0]
        for (index in 0 until items.size) {
            val infos = items[index].split(DeviceModel.ITEM_INFO_SEPARATOR)
            val button = Button(context!!)
            button.text = infos.first()
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            button.setTextColor(textColor)
            button.setBackgroundColor(infos.last().toResColor())
            button.setOnClickListener {
                val newTag = buttonTag.copy(value = infos[1].toInt())
                startActivity<ConfirmActivity>(context) {
                    val title = "${buttonTag.name.toResString()}:${button.text}"
                    putExtra(ConfirmActivity.EXT_TITLE, title)
                    putParcelableArrayListExtra(ConfirmActivity.EXT_TAGS, arrayListOf(newTag))
                }
            }
            layoutParams.setMargins(space)
            button.layoutParams = layoutParams
            container.addView(button)
        }
    }
}
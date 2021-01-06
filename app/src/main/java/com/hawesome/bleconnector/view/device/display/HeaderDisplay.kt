package com.hawesome.bleconnector.view.device

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.model.DevicePageItem
import kotlinx.android.synthetic.main.display_header.view.*

class HeaderDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.display_header, this)
        nameText.text = pageItem.section?.toResString()
        if (pageItem.child.isNullOrEmpty()) {
            imageView.visibility = View.INVISIBLE
        } else {
            imageView.visibility = View.VISIBLE
            setOnClickListener {
                DeviceKit.getPage(pageItem)?.let { page ->
                    startActivity<SecondaryActivity>(context) {
                        putExtra(SecondaryActivity.EXT_PARENT, nameText.text)
                        putExtra(DevicePageFragment.KEY_PAGE_TITLE, page.title)
                        putExtra(DevicePageFragment.KEY_SECONDARY_CHILDREN, pageItem.child)
                    }
                }
            }
        }
    }
}
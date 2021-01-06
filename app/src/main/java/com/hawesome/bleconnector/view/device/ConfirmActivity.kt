package com.hawesome.bleconnector.view.device

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.Tag
import kotlinx.android.synthetic.main.activity_confirm.*

class ConfirmActivity : AppCompatActivity() {

    companion object {
        const val EXT_TITLE = "title"
        const val EXT_TAGS = "tags"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)
        title = intent.getStringExtra(EXT_TITLE)
        val tags = intent.getParcelableArrayListExtra<Tag>(EXT_TAGS) ?: listOf<Tag>()
        cancelBtn.setOnClickListener { finish() }
        confirmBtn.setOnClickListener {
            TagKit.requestTagUpdate(tags)
            finish()
        }
    }
}
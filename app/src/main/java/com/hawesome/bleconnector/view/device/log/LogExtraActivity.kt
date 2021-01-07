package com.hawesome.bleconnector.view.device.log

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.model.LogItemExtra
import kotlinx.android.synthetic.main.activity_log_extra.*

class LogExtraActivity : AppCompatActivity() {

    companion object {
        const val EXT_TITLE = "title"
        const val EXT_DATA = "extras"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_extra)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = intent.getStringExtra(EXT_TITLE)
        intent.getParcelableArrayListExtra<LogItemExtra>(EXT_DATA)?.let {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = LogExtraAdapter(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
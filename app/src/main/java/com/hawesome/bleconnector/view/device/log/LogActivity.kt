package com.hawesome.bleconnector.view.device.log

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.LogKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.LogItem
import com.hawesome.bleconnector.model.LogType
import kotlinx.android.synthetic.main.activity_log_list.*
import kotlin.math.min

class LogActivity : AppCompatActivity() {

    companion object {
        const val EXT_NAME = "name"
        const val EXT_COUNT = "count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var count = intent.getIntExtra(EXT_COUNT, 0)
        intent.getStringExtra(EXT_NAME)?.let {
            title = it.toResString()
            val log = TagKit.getLog(it)
            if (log == null) return@let
            count = min(count,log.count)
            val logItems = mutableListOf<LogItem?>()
            if (!log.didLoaded()) {
                TagKit.requestLogQueryForCount(log.address, count)
            }
            for (index in 0 until count) {
                logItems.add(LogKit.getLogData(log, index))
            }
            val logAdapter = LogAdapter(logItems)
            recyclerView.adapter = logAdapter
            log.indexLoadedLiveData.observe(this) { loadedIndex ->
                for (index in 0 until count){
                    if (logItems[index] == null){
                        logItems[index] = LogKit.getLogData(log, index)
                    }
                }
                logAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
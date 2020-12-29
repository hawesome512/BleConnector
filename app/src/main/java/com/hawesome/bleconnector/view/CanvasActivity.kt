package com.hawesome.bleconnector.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.MediatorLiveData
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.model.Tag
import kotlinx.android.synthetic.main.activity_canvas.*
import kotlinx.android.synthetic.main.item_device.*
import java.util.*
import kotlin.random.Random

class CanvasActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CanvasActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        val tags = listOf("Ia", "Ib", "Ic").map { Tag(it, 0) }
        val mulLiveData = MediatorLiveData<Int>()
        tags.forEach { mulLiveData.addSource(it.valueLiveData) {
            mulLiveData.postValue(it)//.value = it
        } }
        mulLiveData.observe(this) {
            Log.i(TAG, "onCreate: $it")
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    tags.forEach { it.updateValue(Random.nextInt()) }
                }
            }
        }, 0, 3000)
    }
}
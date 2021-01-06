package com.hawesome.bleconnector.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MediatorLiveData
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.ConfirmActivity
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.activity_canvas.*
import kotlinx.android.synthetic.main.display_slider.view.*
import kotlinx.android.synthetic.main.item_device.*
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class CanvasActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CanvasActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        executeBtn.setOnClickListener {
            startActivity<ConfirmActivity>(this)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}
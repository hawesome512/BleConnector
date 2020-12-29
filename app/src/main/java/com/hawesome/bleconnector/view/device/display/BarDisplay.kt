package com.hawesome.bleconnector.view.device

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.graphics.alpha
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResColor
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.ext.trim
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DeviceModel
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag

class BarDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    BarChart(context, attrs), OnTagListener {

    init {
        val height = context.resources.getDimension(R.dimen.large_height).toInt()
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        layoutParams = params

        initChart()
        observeTagUpdate(context, pageItem, ::updateViews)
    }

    /*
    * 坐标+参考线
    * */
    private fun initChart() {

        description.isEnabled = false
        legend.isEnabled = false
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        axisRight.isEnabled = false
        setDrawValueAboveBar(false)
        setScaleEnabled(false)
        setTouchEnabled(false)

        val xItems = pageItem.tags
        if (xItems.isNullOrEmpty()) return
        xAxis.valueFormatter = ItemValueFormatter(xItems)
        xAxis.labelCount = xItems!!.size
        axisLeft.setDrawLimitLinesBehindData(true)
    }

    private fun updateYAxis(){
        val yItems = pageItem.items
        //items:["0","0.01*Ul*Un/yellow","Un/green","0.01*Uo*Un/red","1.3*Un"]
        axisLeft.axisMinimum = 0f
        if (yItems.isNullOrEmpty()) return
        if (yItems.size >= 2) {
            axisLeft.removeAllLimitLines()
            axisLeft.axisMinimum = TagValueConverter.getUnitFactor(yItems.first()).toFloat()
            axisLeft.axisMaximum = TagValueConverter.getUnitFactor(yItems.last()).toFloat()
            for (index in 1 until yItems.size - 1) {
                val infos = yItems[index].split(DeviceModel.ITEM_INFO_SEPARATOR)
                if (infos.size != 2) continue
                val value = TagValueConverter.getUnitFactor(infos[0])
                val limit = LimitLine(value.toFloat())
                limit.lineWidth = 2f
                limit.lineColor = infos[1].toResColor()
                limit.enableDashedLine(20f, 20f, 0f)
                axisLeft.limitLines.add(limit)
            }
        } else if (pageItem.unit != null) {
            val unitTag = TagKit.getTags(listOf(pageItem.unit)).firstOrNull()
            axisLeft.axisMaximum = (unitTag?.value ?: 0).toFloat()
        }
    }

    fun updateViews(tags: List<Tag>) {

        updateYAxis()

        val values = tags.map { it.value }
        val entries =
            values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        //默认值显示在柱状图里面，当值小柱状图太矮时显示在上方
        val above = (values.minOrNull() ?: 0).toFloat() <= axisLeft.axisMaximum * 0.15
        setDrawValueAboveBar(above)
        val set = BarDataSet(entries, "")
        set.color = context.getColor(R.color.blue_200)
        val barData = BarData(set)
        barData.setDrawValues(true)
        val textColorID = if (above) R.color.primary_text else R.color.white
        barData.setValueTextColor(context.getColor(textColorID))
        barData.setValueTextSize(20f)
        barData.setValueFormatter(YValueFormatter())
        data = barData
        invalidate()
    }

    /*
    * X值格式：1.0,2.0,3.0→A相，B相，C相
    * */
    inner class ItemValueFormatter(val items: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt() % items.size
            return items[index].toResString()
        }
    }

    /*
    * Y值格式：清空多余的尾数0
    * */
    inner class YValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float) = value.trim()
    }
}
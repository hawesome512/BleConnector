package com.hawesome.bleconnector.view.device.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResColor
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.ext.trim
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import com.hawesome.bleconnector.view.device.OnTagListener

class RangeDisplay(context: Context, val pageItem: DevicePageItem, attrs: AttributeSet? = null) :
    View(context, attrs), OnTagListener {

    val space = context.resources.getDimension(R.dimen.medium_space)
    val markerColor = context.getColor(R.color.primary_text)
    var tagValue: Float = 0f

    init {
        val height = context.resources.getDimension(R.dimen.medium_height).toInt()
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        layoutParams = params
        observeTagUpdate(context, pageItem, ::updateViews)
    }

    private fun updateViews(tags: List<Tag>) {
        tagValue = TagValueConverter.getShowString(tags[0].value, pageItem).toFloatOrNull() ?: 0f
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val items = pageItem.items
        if (items.isNullOrEmpty() || items.size < 3) return

        val width = canvas!!.width.toFloat()
        val height = canvas!!.height.toFloat()

        val paint = Paint()
        paint.isAntiAlias = true

        //标题
        val titleColor = context.getColor(R.color.blue_500)
        val titleSize = context.resources.getDimension(R.dimen.font_headline)
        paint.setColor(titleColor)
        paint.textSize = titleSize
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(pageItem.name.toResString(), space, space * 2, paint)

        val dividerColor = context.getColor(R.color.divider)
        val dividerWidth = context.resources.getDimension(R.dimen.divider)
        paint.strokeWidth = dividerWidth
        paint.setColor(dividerColor)
        canvas.drawLine(0f, height, width, height, paint)

        val lineX = space
        val lineY = height * 0.7f
        val textY = height - space / 2
        val totalWidth = width - space * 2
        val min = TagValueConverter.getUnitFactor(items.first()).toFloat()
        val max = TagValueConverter.getUnitFactor(items.last()).toFloat()
        for (index in items.size - 1 downTo 2 step 2) {
            val itemValue = TagValueConverter.getUnitFactor(items[index]).toFloat()
            val itemWidth = totalWidth * (itemValue - min) / (max - min)
            drawStep(canvas, lineX, lineY, itemWidth, paint, items[index - 1])
            drawText(canvas, lineX + itemWidth, textY, paint, itemValue.trim())
        }
        drawText(canvas, lineX, textY, paint, min.trim())

        val valueX = totalWidth * (tagValue - min) / (max - min) + lineX
        val valueY = lineY - space / 4
        drawText(canvas, valueX, valueY - space * 1.5f, paint, tagValue.toString())
        drawMarker(canvas, valueX, valueY, paint)
    }

    private fun drawMarker(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x - space / 2, y - space)
        path.lineTo(x + space / 2, y - space)
        path.lineTo(x, y)
        canvas.drawPath(path, paint)
    }

    private fun drawStep(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        width: Float,
        paint: Paint,
        color: String
    ) {
        val lineWidth = space / 2
        val markerWidth = space / 4
        val endX = startX + width

        paint.strokeWidth = lineWidth
        paint.setColor(color.toResColor())
        canvas.drawLine(startX, startY, endX, startY, paint)

        paint.setColor(markerColor)
        canvas.drawLine(startX, startY, startX + markerWidth, startY, paint)
        canvas.drawLine(endX, startY, endX - markerWidth, startY, paint)
    }

    private fun drawText(canvas: Canvas, x: Float, y: Float, paint: Paint, text: String) {
        val textSize = context.resources.getDimension(R.dimen.font_body)
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 1f
        paint.setColor(markerColor)
        canvas.drawText(text, x, y, paint)
    }
}
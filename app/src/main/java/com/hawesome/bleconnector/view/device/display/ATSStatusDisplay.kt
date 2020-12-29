package com.hawesome.bleconnector.view.device

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.toResString
import com.hawesome.bleconnector.kit.TagValueConverter
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.DeviceStatus
import com.hawesome.bleconnector.model.Tag

class ATSStatusDisplay(
    context: Context,
    val pageItem: DevicePageItem,
    attrs: AttributeSet? = null
) : View(context, attrs), OnTagListener {

    companion object {
        //边界距比例:xWidth,xHeight
        const val OFFSET_SPACE_RATIO = 0.1f
        const val SPACE = 20f
        const val MIN_SPACE = SPACE * 0.7f
        const val LINE_WIDTH = 4f

        //断路器符号间隙：xHeight
        const val SWITCH_SPACE_RATIO = 0.15F

        //放在companion object,enum class BreakerStatus才可以调用
        var status = ""
    }

    init {
        val height = context.resources.getDimension(R.dimen.large_height).toInt()
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        layoutParams = params
        observeTagUpdate(context, pageItem, ::updateViews)
    }

    private fun updateViews(tags: List<Tag>) {
        status = TagValueConverter.getBitStatus(tags[0].value, pageItem.items).info ?: ""
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val width = canvas!!.width.toFloat()
        val height = canvas!!.height.toFloat()
        val lineColor = context.getColor(R.color.gray_700)

        val paint = Paint()
        paint.setColor(lineColor)
        paint.strokeWidth = LINE_WIDTH
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true

        val path = Path()
        addVerLines(path, width * OFFSET_SPACE_RATIO, height)
        addVerLines(path, width * (1 - OFFSET_SPACE_RATIO), height)
        val yQT = height * 0.65f
        addHorLines(path, yQT, width, height * SWITCH_SPACE_RATIO)
        canvas.drawPath(path, paint)

        //文本：I电，II电
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 30f
        paint.strokeWidth = 2f
        paint.textScaleX = 1.2f
        val verOffset = height * OFFSET_SPACE_RATIO * 0.75f
        val leftOffset = width * OFFSET_SPACE_RATIO
        val rightOffset = width - leftOffset
        canvas.drawText("Source1".toResString(), leftOffset, verOffset, paint)
        canvas.drawText("Source2".toResString(), rightOffset, verOffset, paint)

        //文本：QT,QS1,QS2
        paint.setColor(context.getColor(R.color.secondary_text))
        val textOffset = SPACE * 2
        canvas.drawText("QT", width * 0.5f, yQT - textOffset, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("QS1", leftOffset + textOffset, height * 0.5f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QS2", rightOffset - textOffset, height * 0.5f, paint)

        //纵向两条线段的端点
        val y1 = height * OFFSET_SPACE_RATIO + SPACE * 3
        val y2 = height * (0.5f - SWITCH_SPACE_RATIO / 2)
        val y3 = height * (0.5f + SWITCH_SPACE_RATIO / 2)
        val y4 = height * (1 - OFFSET_SPACE_RATIO)
        val xQT = width * 0.5f - height * SWITCH_SPACE_RATIO

        //指示灯边框
        paint.setColor(lineColor)
        canvas.drawCircle(leftOffset, (y1 + y2) / 2, SPACE, paint)
        canvas.drawCircle(rightOffset, (y1 + y2) / 2, SPACE, paint)
        canvas.drawCircle(leftOffset, (yQT + y4) / 2, SPACE, paint)
        canvas.drawCircle(rightOffset, (yQT + y4) / 2, SPACE, paint)
        canvas.drawCircle(xQT - SPACE * 3, yQT, SPACE, paint)

        //【动态：位置】开关
        paint.strokeWidth = LINE_WIDTH * 2
        var offset = BreakerStatus.SOURCE1.getOffset()
        canvas.drawLine(leftOffset, y3, leftOffset - offset, y2 + offset, paint)
        offset = BreakerStatus.SOURCE2.getOffset()
        canvas.drawLine(rightOffset, y3, rightOffset - offset, y2 + offset, paint)
        offset = BreakerStatus.TOTIE.getOffset()
        canvas.drawLine(xQT, yQT, width * 0.5f - offset, yQT - offset, paint)

        //【动态：颜色】指示灯
        paint.setColor(context.getColor(R.color.normal))
        val radius = SPACE - LINE_WIDTH * 2
        paint.setColor(BreakerStatus.SOURCE1.getColor())
        canvas.drawCircle(leftOffset, (y1 + y2) / 2, radius, paint)
        paint.setColor(BreakerStatus.SOURCE2.getColor())
        canvas.drawCircle(rightOffset, (y1 + y2) / 2, radius, paint)
        paint.setColor(BreakerStatus.TO1.getColor())
        canvas.drawCircle(leftOffset, (yQT + y4) / 2, radius, paint)
        paint.setColor(BreakerStatus.TO2.getColor())
        canvas.drawCircle(rightOffset, (yQT + y4) / 2, radius, paint)
        paint.setColor(BreakerStatus.TOTIE.getColor())
        canvas.drawCircle(xQT - SPACE * 3, yQT, radius, paint)
    }

    /*
    * QS1/QS2的线：○○--○--x   --------○-----▷
    * */
    private fun addVerLines(path: Path, startX: Float, hei: Float) {
        val startY = hei * OFFSET_SPACE_RATIO
        addCircle(path, startX, startY + SPACE)
        addCircle(path, startX, startY + SPACE * 2)
        val y1 = startY + SPACE * 3
        val y2 = hei * (0.5f - SWITCH_SPACE_RATIO / 2)
        path.moveTo(startX, y1)
        path.lineTo(startX, y2)
        path.moveTo(startX - MIN_SPACE, y2 - MIN_SPACE)
        path.lineTo(startX + MIN_SPACE, y2 + MIN_SPACE)
        path.moveTo(startX + MIN_SPACE, y2 - MIN_SPACE)
        path.lineTo(startX - MIN_SPACE, y2 + MIN_SPACE)
        path.moveTo(startX, y2 + hei * SWITCH_SPACE_RATIO)
        val bottomY = hei * 0.9f
        path.lineTo(startX, bottomY)
        path.moveTo(startX - SPACE, bottomY)
        path.lineTo(startX, bottomY + SPACE * 1.5f)
        path.lineTo(startX + SPACE, bottomY)
        path.lineTo(startX - SPACE, bottomY)
    }

    /*
    * QT的线：------------   x---------------
    * */
    private fun addHorLines(path: Path, startY: Float, width: Float, swSpace: Float) {
        val startX = width * OFFSET_SPACE_RATIO
        val centerX = width * 0.5f
        path.moveTo(startX, startY)
        path.lineTo(centerX - swSpace, startY)
        path.moveTo(centerX - MIN_SPACE, startY - MIN_SPACE)
        path.lineTo(centerX + MIN_SPACE, startY + MIN_SPACE)
        path.moveTo(centerX + MIN_SPACE, startY - MIN_SPACE)
        path.lineTo(centerX - MIN_SPACE, startY + MIN_SPACE)
        path.moveTo(centerX, startY)
        path.lineTo(width * (1 - OFFSET_SPACE_RATIO), startY)
    }

    private fun addCircle(path: Path, centerX: Float, centerY: Float) {
        path.addCircle(centerX, centerY, SPACE, Path.Direction.CW)
    }

    enum class BreakerStatus {
        SOURCE1 {
            override fun getColor(): Int {
                val deviceStatus =
                    if (status.contains("1fault")) DeviceStatus.ALARM else DeviceStatus.ON
                return deviceStatus.toColor()
            }

            override fun getOffset() = if (status.contains("1on")) 0f else SPACE
        },
        SOURCE2 {
            override fun getColor(): Int {
                val deviceStatus =
                    if (status.contains("2fault")) DeviceStatus.ALARM else DeviceStatus.ON
                return deviceStatus.toColor()
            }

            override fun getOffset() = if (status.contains("2on")) 0f else SPACE
        },
        TO1 {
            override fun getColor(): Int {
                val deviceStatus = if (status.contains("1trip")) DeviceStatus.ALARM
                else if (status.contains("1on") ||
                    (status.contains("2on") && status.contains("tieon"))
                ) DeviceStatus.ON
                else DeviceStatus.OFF
                return deviceStatus.toColor()
            }

            override fun getOffset() = 0f

        },
        TO2 {
            override fun getColor(): Int {
                val deviceStatus = if (status.contains("2trip")) DeviceStatus.ALARM
                else if (status.contains("2on") ||
                    (status.contains("1on") && status.contains("tieon"))
                ) DeviceStatus.ON
                else DeviceStatus.OFF
                return deviceStatus.toColor()
            }

            override fun getOffset() = 0f

        },
        TOTIE {
            override fun getColor(): Int {
                val deviceStatus = if (status.contains("tietrip")) DeviceStatus.ALARM
                else if (status.contains("tieon") &&
                    (status.contains("1on") || status.contains("2on"))
                ) DeviceStatus.ON
                else DeviceStatus.OFF
                return deviceStatus.toColor()
            }

            override fun getOffset() = if (status.contains("tieon")) 0f else SPACE

        };

        abstract fun getColor(): Int
        abstract fun getOffset(): Float
    }
}
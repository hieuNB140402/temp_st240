package com.meskiep.vaithat.core.custom.text

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class StrokeSpan(
    private val strokeColor: Int,
    private val strokeWidth: Float,
    private val strokeJoin: Paint.Join = Paint.Join.ROUND,
    private val strokeMiter: Float = 5f
) : ReplacementSpan() {

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int,
                      x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        // Lưu state gốc
        val originalStyle = paint.style
        val originalColor = paint.color
        val originalWidth = paint.strokeWidth
        val originalJoin = paint.strokeJoin
        val originalMiter = paint.strokeMiter

        // Vẽ stroke
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
        paint.strokeJoin = strokeJoin
        paint.strokeMiter = strokeMiter
        canvas.drawText(text ?: "", start, end, x, y.toFloat(), paint)

        // Vẽ fill (giữ màu gốc của span ForegroundColorSpan)
        paint.style = Paint.Style.FILL
        paint.color = originalColor
        canvas.drawText(text ?: "", start, end, x, y.toFloat(), paint)

        // Restore
        paint.style = originalStyle
        paint.strokeWidth = originalWidth
        paint.strokeJoin = originalJoin
        paint.strokeMiter = originalMiter
    }
}
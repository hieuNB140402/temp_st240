package com.meskiep.vaithat.core.custom.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.meskiep.vaithat.R

class StrokeTextViewGradient: AppCompatTextView {

    private var strokeWidth = 0f
    private var strokeColor: Int = Color.WHITE
    private var strokeJoin: Paint.Join = Paint.Join.ROUND
    private var strokeMiter = 0f

    private var startColor: Int = Color.WHITE
    private var endColor: Int = Color.WHITE

    private var gradient: LinearGradient? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @SuppressLint("Recycle")
    private fun init(attrs: AttributeSet?) {
        attrs ?: return

        val a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)

        strokeWidth = a.getDimension(R.styleable.StrokeTextView_strokeWidth, 1.5f)
        strokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, Color.WHITE)
        strokeMiter = a.getDimension(R.styleable.StrokeTextView_strokeMiter, 5f)

        strokeJoin = when (a.getInt(R.styleable.StrokeTextView_strokeJoinStyle, 2)) {
            0 -> Paint.Join.MITER
            1 -> Paint.Join.BEVEL
            else -> Paint.Join.ROUND
        }

        startColor = a.getColor(R.styleable.StrokeTextView_startColor, Color.WHITE)
        endColor = a.getColor(R.styleable.StrokeTextView_endColor, Color.WHITE)

        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > 0) {
            gradient = LinearGradient(
                0f, 0f,
                0f, h.toFloat(),
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        val paint = paint
        val oldTextColor = currentTextColor
        // ===== Fill (Gradient) =====
        paint.style = Paint.Style.FILL
        paint.shader = gradient
        super.onDraw(canvas)

        // ===== Stroke =====
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = strokeJoin
        paint.strokeMiter = strokeMiter
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isSubpixelText = true

        setTextColor(strokeColor)   // 🔥 BẮT BUỘC
        super.onDraw(canvas)

        // ===== Fill (Gradient) =====
        paint.style = Paint.Style.FILL
        paint.shader = gradient

        setTextColor(Color.WHITE)   // màu này sẽ bị shader ghi đè
        super.onDraw(canvas)

        setTextColor(oldTextColor)
    }

    // Helper nếu muốn set nhanh bằng code
    fun setGradientText(start: Int, end: Int) {
        startColor = start
        endColor = end
        requestLayout()
        invalidate()
    }
}
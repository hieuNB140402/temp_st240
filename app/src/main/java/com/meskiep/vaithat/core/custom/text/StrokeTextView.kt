package com.meskiep.vaithat.core.custom.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.meskiep.vaithat.R

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidth = 0f
    private var strokeColor: Int = 0
    private var strokeJoin: Paint.Join = Paint.Join.ROUND
    private var strokeMiter = 5f

    /** True khi stroke đã được cấu hình (strokeWidth > 0) */
    private val hasStroke get() = strokeWidth > 0f

    init {
        initAttrs(attrs)
    }

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?) {
        attrs ?: return
        val a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
        if (a.hasValue(R.styleable.StrokeTextView_strokeColor)) {
            strokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeWidth, 1).toFloat()
            strokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, currentTextColor)
            strokeMiter = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeMiter, 5).toFloat()
            strokeJoin = when (a.getInt(R.styleable.StrokeTextView_strokeJoinStyle, 2)) {
                1 -> Paint.Join.BEVEL
                2 -> Paint.Join.ROUND
                else -> Paint.Join.MITER
            }
        }
        a.recycle()
    }

    /**
     * Cấu hình stroke bằng code.
     * @param width  Độ rộng stroke (px). Truyền 0f để tắt stroke.
     * @param color  Màu stroke.
     * @param join   Kiểu nối góc (mặc định ROUND cho chữ đẹp hơn).
     * @param miter  Giới hạn miter (chỉ có tác dụng khi join = MITER).
     */
    fun setStroke(width: Float, color: Int, join: Paint.Join = Paint.Join.ROUND, miter: Float = 5f) {
        strokeWidth = width
        strokeColor = color
        strokeJoin = join
        strokeMiter = miter
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!hasStroke) {
            // Không có stroke → vẽ bình thường
            super.onDraw(canvas)
            return
        }

        val textColor = currentTextColor

        // ── Bước 1: vẽ stroke ──────────────────────────────────────────────
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = this@StrokeTextView.strokeWidth
            strokeJoin = this@StrokeTextView.strokeJoin
            strokeMiter = this@StrokeTextView.strokeMiter
        }
        setTextColor(strokeColor)
        super.onDraw(canvas)

        // ── Bước 2: vẽ fill đè lên ─────────────────────────────────────────
        paint.apply {
            style = Paint.Style.FILL
            strokeWidth = 0f          // Reset để tránh ảnh hưởng fill pass
        }
        setTextColor(textColor)
        super.onDraw(canvas)
    }
}
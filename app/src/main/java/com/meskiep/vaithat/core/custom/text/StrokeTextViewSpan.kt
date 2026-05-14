package com.meskiep.vaithat.core.custom.text

import android.R.attr.text
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.meskiep.vaithat.R

class StrokeTextViewSpan @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidth = 0f
    private var strokeColor: Int = Color.WHITE
    private var strokeJoin: Paint.Join = Paint.Join.ROUND
    private var strokeMiter = 5f

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?) {
        attrs ?: return
        val a = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
        if (a.hasValue(R.styleable.StrokeTextView_strokeColor)) {
            strokeWidth = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeWidth, 1).toFloat()
            strokeColor = a.getColor(R.styleable.StrokeTextView_strokeColor, Color.BLACK)
            strokeMiter = a.getDimensionPixelSize(R.styleable.StrokeTextView_strokeMiter, 10).toFloat()
            strokeJoin = when (a.getInt(R.styleable.StrokeTextView_strokeJoinStyle, 0)) {
                1 -> Paint.Join.BEVEL
                2 -> Paint.Join.ROUND
                else -> Paint.Join.MITER
            }
        }
        a.recycle()
    }

    fun setStroke(width: Float, color: Int, join: Paint.Join = Paint.Join.ROUND, miter: Float = 5f) {
        strokeWidth = width
        strokeColor = color
        strokeJoin = join
        strokeMiter = miter
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (strokeWidth <= 0f || layout == null) {
            super.onDraw(canvas)
            return
        }

        val textLayout = layout
        val paint = paint

        canvas.save()
        // Translate đúng padding
        canvas.translate(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat())

        // ── Pass 1: Vẽ stroke với màu stroke, giữ nguyên span khác ──
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeJoin = strokeJoin
        paint.strokeMiter = strokeMiter

        // Build spannable stroke: clone text, đổi tất cả ForegroundColorSpan → strokeColor
        val strokeSpannable = buildStrokeSpannable()
        val strokeLayout = buildStaticLayout(strokeSpannable, paint, textLayout)
        strokeLayout.draw(canvas)

        // ── Pass 2: Vẽ fill với text gốc (giữ nguyên ForegroundColorSpan) ──
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        textLayout.draw(canvas)

        canvas.restore()
    }

    /**
     * Clone text hiện tại, thay toàn bộ ForegroundColorSpan bằng strokeColor
     */
    private fun buildStrokeSpannable(): SpannableStringBuilder {
        val original = text ?: return SpannableStringBuilder()
        val ssb = SpannableStringBuilder(original)

        // Xóa ForegroundColorSpan cũ
        ssb.getSpans(0, ssb.length, ForegroundColorSpan::class.java)
            .forEach { ssb.removeSpan(it) }

        // Set toàn bộ text = strokeColor
        ssb.setSpan(
            ForegroundColorSpan(strokeColor),
            0, ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ssb
    }

    private fun buildStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        referenceLayout: Layout
    ): StaticLayout {
        val width = referenceLayout.width
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(referenceLayout.alignment)
            .setLineSpacing(referenceLayout.spacingAdd, referenceLayout.spacingMultiplier)
            .setIncludePad(true)
            .build()
    }
}
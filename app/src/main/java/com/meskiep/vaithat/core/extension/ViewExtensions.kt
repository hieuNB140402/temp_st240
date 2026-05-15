package com.meskiep.vaithat.core.extension

import android.R.attr.gravity
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.helper.UnitHelper
import com.meskiep.vaithat.core.utils.DataLocal

// ----------------------------
// Visibility extensions
// ----------------------------
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.select() {
    isSelected = true
}

fun selectText(list: List<TextView>) {
    list.forEach {
        it.select()
    }
}


// ----------------------------
// Navigation / Transition
// ----------------------------
fun Activity.handleBackLeftToRight() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

fun Activity.handleBackRightToLeft() {
    finish()
    overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
}

fun Context.handleBackFragmentFromRight() {
    if (this is FragmentActivity) {
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        supportFragmentManager.popBackStack()
    }
}

fun Activity.hideNavigation(isBlack: Boolean = false) {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    window.decorView.systemUiVisibility = if (isBlack) {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    } else {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

// ----------------------------
// Click utils
// ----------------------------
fun View.tap(interval: Long = 200, action: (View) -> Unit) {
    setOnClickListener {
        if (System.currentTimeMillis() - DataLocal.lastClickTime >= interval) {
            action(it)
            DataLocal.lastClickTime = System.currentTimeMillis()
        }
    }
}

fun View.tapWithSound(interval: Long = 500, action: (View) -> Unit) {
    setOnClickListener {
        if (System.currentTimeMillis() - DataLocal.lastClickTime >= interval) {
            action(it)
            DataLocal.lastClickTime = System.currentTimeMillis()
        }
    }
}

// ----------------------------
// UI Capture
// ----------------------------
@Throws(OutOfMemoryError::class)
fun createBitmapFromView(view: View): Bitmap {
    return try {
        val output = createBitmap(view.width, view.height)
        val canvas = Canvas(output)
        view.draw(canvas)
        output
    } catch (error: OutOfMemoryError) {
        throw error
    }
}

// ----------------------------
// TextView
// ----------------------------
fun TextView.setFont(@FontRes resId: Int) {
    typeface = ResourcesCompat.getFont(context, resId)
}

fun TextView.setTextContent(context: Context, resId: Int) {
    text = context.getString(resId)
}

fun Context.strings(resId: Int): String {
    return getString(resId)
}

fun Context.strings(resId: Int, value: String): String {
    return getString(resId, value)
}

fun ImageView.setImageWithOption(res: Int) {
    this.setImageResource(res)
    this.visible()
}

fun View.setBackgroundWithOption(res: Int) {
    this.setBackgroundResource(res)
    this.visible()
}

fun TextView.setTextWithOption(text: String, isCenter: Boolean = true) {
    this.text = text
    this.visible()
    this.select()
    this.gravity = if (isCenter) {
        Gravity.CENTER
    } else {
        Gravity.START or Gravity.CENTER_VERTICAL
    }
}

fun View.margin(margin: String, distance: Int) {
    val params = this.layoutParams as ViewGroup.MarginLayoutParams
    val distance = UnitHelper.pxToDpInt(this.context, distance)

    when (margin) {
        "top" -> params.topMargin = distance
        "left" -> params.leftMargin = distance
        "right" -> params.rightMargin = distance
        "bottom" -> params.bottomMargin = distance
        "horizontal" -> {
            params.leftMargin = distance
            params.rightMargin = distance
        }

        else -> {
            params.topMargin = distance
            params.leftMargin = distance
            params.rightMargin = distance
            params.bottomMargin = distance
        }
    }

    this.layoutParams = params
}

fun ImageView.setState(isEnable: Boolean, res: Int) {
    this.isEnabled = isEnable
    this.setImageResource(res)
}


fun RecyclerView.addOnItemClickOutSide(event: (() -> Unit)) {
    this.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(
            recyclerView: RecyclerView, motionEvent: MotionEvent
        ): Boolean {
            return when {
                motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                    motionEvent.x, motionEvent.y
                ) != null -> false

                else -> {
                    event.invoke()
                    true
                }
            }
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}

fun Activity.hideNavigationFullScreen(isLightStatusBar: Boolean = false) {
    window.apply {
        // 1. Yêu cầu Window tràn ra toàn màn hình, không bị giới hạn bởi flag cũ
        WindowCompat.setDecorFitsSystemWindows(this, false)

        // 2. Thiết lập màu trong suốt cho cả 2 thanh
        statusBarColor = Color.TRANSPARENT
        navigationBarColor = Color.TRANSPARENT

        // 3. Xử lý cho các máy có "tai thỏ" (Notch) - Đây là nguyên nhân chính gây nền đen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    // 4. Sử dụng WindowInsetsController để ẩn thanh và chỉnh màu icon
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.apply {
        // Ẩn thanh trạng thái và thanh điều hướng
        hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())

        // Chế độ vuốt để hiện lại (tương đương IMMERSIVE_STICKY)
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Chỉnh màu icon (trắng/đen) tùy thuộc vào isLightStatusBar
        isAppearanceLightStatusBars = isLightStatusBar
        isAppearanceLightNavigationBars = isLightStatusBar
    }
}

fun String.capitalizeFirst(): String {
    if (this.isBlank()) return this

    return this.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}
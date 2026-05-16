package com.meskiep.vaithat.core.extension

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd

// Hiệu ứng nhấn (scale nhỏ rồi về lại)
fun View.animateScaleEffect(scaleDownFactor: Float = 0.9f, duration: Long = 100L) {
    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleDownFactor)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleDownFactor)
    scaleDownX.duration = duration
    scaleDownY.duration = duration

    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", scaleDownFactor, 1f)
    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", scaleDownFactor, 1f)
    scaleUpX.duration = duration
    scaleUpY.duration = duration

    AnimatorSet().apply {
        play(scaleDownX).with(scaleDownY)
        play(scaleUpX).with(scaleUpY).after(scaleDownX)
        start()
    }
}

// Hiệu ứng nhấn (scale lớn rồi về lại)
fun View.animateScaleOutEffect(scaleUpFactor: Float = 1.1f, duration: Long = 100L) {
    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleUpFactor)
    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleUpFactor)
    scaleUpX.duration = duration
    scaleUpY.duration = duration

    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", scaleUpFactor, 1f)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", scaleUpFactor, 1f)
    scaleDownX.duration = duration
    scaleDownY.duration = duration

    AnimatorSet().apply {
        play(scaleUpX).with(scaleUpY)
        play(scaleDownX).with(scaleDownY).after(scaleUpX)
        start()
    }
}

// Rung ngang
fun View.shakeViewEffect(duration: Long = 100, repeatCount: Int = 3, shakeDistance: Float = 5f) {
    val animator =
        ObjectAnimator.ofFloat(this, "translationX", -shakeDistance, shakeDistance).apply {
            this.duration = duration
            this.repeatCount = repeatCount
            this.repeatMode = ObjectAnimator.REVERSE
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    this@shakeViewEffect.translationX = 0f
                }
            })
        }
    animator.start()
}

// Quay vòng vô hạn
fun View.startRotationInfinity(duration: Long = 500) {
    ObjectAnimator.ofFloat(this, "rotation", 0f, -360f).apply {
        this.duration = duration
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.RESTART
        start()
    }
}

// Quay 1 vòng
fun View.startRotation(durationEnd: Long = 500, action: () -> Unit) {
    val anim = ObjectAnimator.ofFloat(this, "rotation", 0f, -360f).apply {
        duration = durationEnd
    }
    anim.doOnEnd {
        action.invoke()
    }
    anim.start()
}

// Zoom out từ 0 -> end, kèm callback khi kết thúc
fun View.animateZoom(scaleEnd: Float, duration: Long, onEnd: (() -> Unit)? = null) {
    this.visible()

    val scaleXAnimation = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, scaleEnd)
    val scaleYAnimation = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, scaleEnd)

    AnimatorSet().apply {
        playTogether(scaleXAnimation, scaleYAnimation)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { doOnEnd { it() } }
        start()
    }
}

// Zoom in từ start -> 1f
fun View.animateZoomIn(scaleStart: Float, scaleEnd: Float, duration: Long, onEnd: (() -> Unit)? = null) {
    val scaleXAnimation = ObjectAnimator.ofFloat(this, View.SCALE_X, scaleStart, scaleEnd)
    val scaleYAnimation = ObjectAnimator.ofFloat(this, View.SCALE_Y, scaleStart, scaleEnd)

    AnimatorSet().apply {
        playTogether(scaleXAnimation, scaleYAnimation)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { doOnEnd { it() } }
        start()
    }
}

fun View.animateBurstOut(duration: Long = 1000L) {
    this.scaleX = 1f
    this.scaleY = 1f
    this.alpha = 1f

    this.animate()
        .scaleX(1.5f)
        .scaleY(1.5f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction {
            animateBurstIn(duration)
        }
        .start()
}

fun View.animateBurstIn(duration: Long = 1000L) {
    this.scaleX = 1.5f
    this.scaleY = 1.5f
    this.alpha = 1f

    this.animate()
        .scaleX(1f)
        .scaleY(1f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction {
            animateBurstOut(duration)
        }
        .start()
}

fun View.expandView(startHeight: Int, endHeight: Int) {
    this.visible()

    val animator = ValueAnimator.ofInt(startHeight, endHeight)
    animator.duration = 300

    animator.addUpdateListener {
        val value = it.animatedValue as Int
        this.layoutParams.height = value
        this.requestLayout()
    }

    animator.start()
}

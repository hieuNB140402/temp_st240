package com.meskiep.vaithat.core.helper

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View

class DragBottomSheetHelper(
    private val bottomView: View,
    private val dragView: View
) {

    private var downY = 0f
    private var startTranslationY = 0f
    private var maxTranslationY = 0f

    fun setup() {
        bottomView.post {
            maxTranslationY = bottomView.height.toFloat()
        }

        dragView.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    startTranslationY = bottomView.translationY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = event.rawY - downY
                    val newY = (startTranslationY + delta)
                        .coerceIn(0f, maxTranslationY)

                    bottomView.translationY = newY
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val shouldExpand = bottomView.translationY < maxTranslationY / 2
                    if (shouldExpand) {
                        expand()
                    } else {
                        collapse()
                    }
                    true
                }

                else -> false
            }
        }
    }

    fun expand() {
        animateTo(0f)
    }

    fun collapse() {
        animateTo(maxTranslationY)
    }

    private fun animateTo(target: Float) {
        val animator = ValueAnimator.ofFloat(bottomView.translationY, target)
        animator.duration = 250
        animator.addUpdateListener {
            bottomView.translationY = it.animatedValue as Float
        }
        animator.start()
    }
}
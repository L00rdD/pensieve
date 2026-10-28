package com.l0rdd.pensieve.presentation.views.common

import android.content.Context
import android.widget.FrameLayout

/**
 * Lays its one child out at the design size, 1920 × 1080 pixels, and scales it to the screen,
 * so every look is drawn with the mockups' coordinates whatever the projector's resolution.
 */
class Stage(context: Context) : FrameLayout(context) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
        val exact = { size: Int -> MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY) }
        for (i in 0 until childCount) getChildAt(i).measure(exact(WIDTH), exact(HEIGHT))
        val scale = minOf(w / WIDTH.toFloat(), h / HEIGHT.toFloat())
        for (i in 0 until childCount) getChildAt(i).apply {
            pivotX = 0f; pivotY = 0f; scaleX = scale; scaleY = scale
            translationX = (w - WIDTH * scale) / 2; translationY = (h - HEIGHT * scale) / 2
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) getChildAt(i).layout(0, 0, WIDTH, HEIGHT)
    }

    companion object {
        const val WIDTH = 1920
        const val HEIGHT = 1080
    }
}

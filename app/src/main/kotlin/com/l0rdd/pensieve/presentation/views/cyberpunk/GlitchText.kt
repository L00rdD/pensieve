package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock

/** Text drawn three times, red and cyan shifted under the main colour, glitching now and then. */
class GlitchText(context: Context, face: Typeface, size: Float, private val main: Int, clock: FrameClock?) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = size; letterSpacing = 0.02f }
    var text: String = ""
        set(value) { if (field != value) { field = value; requestLayout(); invalidate() } }
    private var jolt = 0f

    init {
        clock?.add { t, _ -> val c = t % 3f; val j = if (c in 2.7f..2.8f) -6f else if (c in 2.8f..2.9f) 5f else 0f; if (j != jolt) { jolt = j; invalidate() } }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fm = paint.fontMetrics
        setMeasuredDimension(paint.measureText(text).toInt() + 12, (fm.descent - fm.ascent).toInt() + 4)
    }

    override fun onDraw(canvas: Canvas) {
        val y = -paint.fontMetrics.ascent
        paint.color = CyberColors.alpha(CyberColors.RED, 0.85f); canvas.drawText(text, 3f + jolt, y, paint)
        paint.color = CyberColors.alpha(CyberColors.CYAN, 0.8f); canvas.drawText(text, 9f - jolt, y, paint)
        paint.color = main; canvas.drawText(text, 6f, y, paint)
    }
}

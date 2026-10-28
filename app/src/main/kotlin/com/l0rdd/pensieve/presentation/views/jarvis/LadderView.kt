package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock

/** An altitude or thrust ladder on a side of the helmet, decoration only. */
class LadderView(context: Context, typeface: Typeface, clock: FrameClock, private val label: String, private val value: String, private val speed: Float) : View(context) {
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.typeface = typeface; textSize = 15f; textAlign = Paint.Align.CENTER; letterSpacing = 0.25f }
    private val tick = Paint().apply { strokeWidth = 1f }
    private var shift = 0f

    init {
        clock.add { t, _ -> shift = (t * speed) % 14f; invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        text.color = JarvisColors.MUTED
        canvas.drawText(label, cx, 16f, text)
        val top = 34f; val bottom = top + 300f
        var y = top + shift
        while (y < bottom) {
            val fade = minOf((y - top) / 60f, (bottom - y) / 60f, 1f)
            tick.color = JarvisColors.alpha(JarvisColors.CYAN, 0.5f * fade)
            canvas.drawLine(cx - 10, y, cx + 10, y, tick)
            y += 14f
        }
        text.color = JarvisColors.GOLD
        canvas.drawText(value, cx, bottom + 30f, text)
    }
}

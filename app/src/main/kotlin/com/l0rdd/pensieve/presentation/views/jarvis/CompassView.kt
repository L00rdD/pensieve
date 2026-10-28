package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.math.abs
import kotlin.math.cos

/** The heading tape at the top, drifting slowly as if the head turned. */
class CompassView(context: Context, typeface: Typeface, clock: FrameClock) : View(context) {
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.typeface = typeface; textSize = 15f; textAlign = Paint.Align.CENTER; letterSpacing = 0.1f }
    private val tick = Paint().apply { strokeWidth = 1f }
    private val marker = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JarvisColors.GOLD }
    private var offset = 0f

    init {
        clock.add { t, _ -> offset = ((1 - cos(t / 26f * Math.PI.toFloat())) / 2) * 560f; invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val mid = w / 2
        for (i in 0 until 26) {
            val x = i * 64f - offset + 32f
            val fade = (1 - abs(x - mid) / mid).coerceIn(0f, 1f) * 1.6f
            if (fade <= 0f) continue
            tick.color = JarvisColors.alpha(JarvisColors.CYAN, 0.6f * fade.coerceAtMost(1f))
            canvas.drawLine(x, 0f, x, 12f, tick)
            canvas.drawLine(x - 32, 0f, x - 32, 6f, tick)
            val h = (i * 15) % 360
            text.color = JarvisColors.alpha(JarvisColors.MUTED, fade.coerceAtMost(1f))
            canvas.drawText(mapOf(0 to "N", 90 to "E", 180 to "S", 270 to "O")[h] ?: "%03d".format(h), x, 33f, text)
        }
        canvas.drawPath(Path().apply { moveTo(mid - 7, height.toFloat()); lineTo(mid + 7, height.toFloat()); lineTo(mid, height - 9f); close() }, marker)
    }
}

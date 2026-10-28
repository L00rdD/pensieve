package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

/** The line from a module to the reactor, with its dot of light at the reactor's end. */
class CalloutView(context: Context, private val dotAtRight: Boolean) : View(context) {
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JarvisColors.alpha(JarvisColors.CYAN, 0.5f); strokeWidth = 1f }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JarvisColors.CYAN; setShadowLayer(8f, 0f, 0f, JarvisColors.CYAN) }

    override fun onDraw(canvas: Canvas) {
        val y = height / 2f
        canvas.drawLine(0f, y, width.toFloat(), y, line)
        canvas.drawCircle(if (dotAtRight) width - 4f else 4f, y, 3.5f, dot)
    }
}

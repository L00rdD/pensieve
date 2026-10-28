package com.l0rdd.pensieve.presentation.views.jarvis

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader

/** The HUD frame: a faint glass pane with corner brackets of light. */
object Brackets {
    private val fill = Paint()
    private val edge = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f }
    private val corner = Paint().apply { strokeWidth = 2f }

    fun draw(canvas: Canvas, w: Float, h: Float, color: Int, size: Float = 18f, all: Boolean = true) {
        fill.shader = LinearGradient(0f, 0f, w, h, JarvisColors.alpha(JarvisColors.CYAN, 0.11f), JarvisColors.alpha(JarvisColors.CYAN, 0.02f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, fill)
        edge.color = JarvisColors.alpha(color, 0.16f)
        canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, edge)
        corner.color = color
        fun l(x: Float, y: Float, dx: Float, dy: Float) { canvas.drawLine(x, y, x + dx, y, corner); canvas.drawLine(x, y, x, y + dy, corner) }
        l(1f, 1f, size, size); l(w - 1, h - 1, -size, -size)
        if (all) { l(w - 1, 1f, -size, size); l(1f, h - 1, size, -size) }
    }
}

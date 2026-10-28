package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/** Cyberpunk's panel: a thin red frame with its bottom right corner cut and a red edge on the left. */
object CutFrame {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f }
    private val side = Paint()

    fun path(w: Float, h: Float, cut: Float = 16f) = Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h - cut); lineTo(w - cut, h); lineTo(0f, h); close() }

    fun draw(canvas: Canvas, w: Float, h: Float, focused: Boolean, cut: Float = 16f) {
        val path = path(w, h, cut)
        fill.color = if (focused) CyberColors.alpha(CyberColors.YELLOW, 0.1f) else CyberColors.alpha(0xFF24080C.toInt(), 0.55f)
        canvas.drawPath(path, fill)
        edge.color = if (focused) CyberColors.YELLOW else CyberColors.alpha(CyberColors.RED, 0.35f)
        canvas.drawPath(path, edge)
        side.color = if (focused) CyberColors.YELLOW else CyberColors.RED
        canvas.drawRect(0f, 0f, if (focused) 6f else 3f, h, side)
    }
}

package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View

/** Scanlines and a dark vignette over the screen. */
class CyberOverlay(context: Context) : View(context) {
    private val line = Paint().apply { color = 0x06FFFFFF }
    private val vignette = Paint()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        var y = 3f; while (y < h) { canvas.drawRect(0f, y, w, y + 1, line); y += 4f }
        vignette.shader = RadialGradient(w / 2, h / 2, w * 0.62f, intArrayOf(0, 0, 0x99000000.toInt()), floatArrayOf(0f, 0.58f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, vignette)
    }
}

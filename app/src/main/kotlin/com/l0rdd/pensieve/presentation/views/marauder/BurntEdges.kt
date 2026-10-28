package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View

/** The parchment's burnt edges, darkening towards the corners. */
class BurntEdges(context: Context) : View(context) {
    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        paint.shader = RadialGradient(w / 2, h / 2, w * 0.62f, intArrayOf(0, 0, InkColors.alpha(0xFF6E320A.toInt(), 0.35f), InkColors.alpha(0xFF461A05.toInt(), 0.72f)), floatArrayOf(0f, 0.72f, 0.9f, 1f), Shader.TileMode.CLAMP)
        canvas.save(); canvas.scale(1f, h / w * 1.1f, w / 2, h / 2)
        canvas.drawRect(0f, -h, w, h * 2, paint)
        canvas.restore()
    }
}

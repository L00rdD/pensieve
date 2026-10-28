package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.CYAN
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.GOLD
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.alpha

/** The inside of the helmet, drawn once: the grid, the glow in the middle, the curved frame in the corners. */
class HelmetView(context: Context) : View(context) {
    private val grid = Paint().apply { color = alpha(CYAN, 0.045f); strokeWidth = 1f }
    private val glow = Paint()
    private val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; color = alpha(CYAN, 0.45f) }
    private val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; color = alpha(GOLD, 0.55f) }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        canvas.drawColor(JarvisColors.BG)
        glow.shader = RadialGradient(w / 2, h * 0.42f, w * 0.45f, alpha(0xFF2896C3.toInt(), 0.22f), 0, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, glow)
        var x = 0f; while (x < w) { canvas.drawLine(x, 0f, x, h, grid); x += 44f }
        var y = 0f; while (y < h) { canvas.drawLine(0f, y, w, y, grid); y += 44f }
        listOf(
            Path().apply { moveTo(40f, 300f); quadTo(40f, 60f, 300f, 40f) },
            Path().apply { moveTo(w - 40, 300f); quadTo(w - 40, 60f, w - 300, 40f) },
            Path().apply { moveTo(40f, 780f); quadTo(40f, h - 40, 300f, h - 30) },
            Path().apply { moveTo(w - 40, 780f); quadTo(w - 40, h - 40, w - 300, h - 30) },
        ).forEach { canvas.drawPath(it, frame) }
        canvas.drawPath(Path().apply { moveTo(70f, 330f); quadTo(75f, 95f, 330f, 70f) }, gold)
        canvas.drawPath(Path().apply { moveTo(w - 70, 330f); quadTo(w - 75, 95f, w - 330, 70f) }, gold)
    }
}

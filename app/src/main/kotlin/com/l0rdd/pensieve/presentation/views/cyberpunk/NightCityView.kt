package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.random.Random

/** Night City behind the menus: a grid running to the horizon, falling columns of data, a neon sign. */
class NightCityView(context: Context, private val face: Typeface, private val sign: String, clock: FrameClock) : View(context) {
    private val glow = Paint()
    private val grid = Paint().apply { color = CyberColors.alpha(CyberColors.RED, 0.15f); strokeWidth = 1f }
    private val rain = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 18f }
    private val neon = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 44f; color = CyberColors.RED; setShadowLayer(18f, 0f, 0f, 0xFFFF2A1F.toInt()) }
    private val random = Random(3)
    private val columns = List(30) { floatArrayOf(random.nextFloat() * 1920, random.nextFloat() * 1080, 40 + random.nextFloat() * 80, 6f + random.nextInt(12)) }
    private var t = 0f

    init {
        clock.add { time, dt ->
            t = time
            columns.forEach { c -> c[1] += c[2] * dt; if (c[1] > 1180) { c[1] = -200f; c[0] = random.nextFloat() * 1920 } }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        canvas.drawColor(CyberColors.BG)
        glow.shader = RadialGradient(w * 0.68f, h * 0.32f, w * 0.6f, 0xFF1E0A12.toInt(), CyberColors.BG, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, glow)
        val horizon = 790f
        glow.shader = LinearGradient(0f, horizon - 140, 0f, horizon, 0, CyberColors.alpha(CyberColors.RED, 0.14f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, horizon - 140, w, horizon, glow)
        for (i in -26..26) canvas.drawLine(960f + i * 28, horizon, 960f + i * 170, h, grid)
        val off = (t * 40) % 60
        for (z in 0 until 12) { val y = horizon + ((z * 60 + off) / 720f).let { it * it } * 290; canvas.drawLine(0f, y, w, y, grid) }
        columns.forEach { c ->
            for (k in 0 until c[3].toInt()) {
                rain.color = if (k == 0) CyberColors.alpha(0xFFC8FFFF.toInt(), 0.3f) else CyberColors.alpha(CyberColors.CYAN, 0.12f * (1 - k / c[3]))
                canvas.drawText((0x30A0 + ((k * 7 + (c[1] / 22).toInt()) % 90)).toChar().toString(), c[0], c[1] - k * 22, rain)
            }
        }
        // the sign flickers now and then
        val cycle = t % 6f
        neon.alpha = if (cycle in 1.1f..1.3f || cycle in 3.7f..3.8f) 50 else 190
        var y = 200f
        sign.forEach { ch -> canvas.drawText(ch.toString(), w - 70f, y, neon); y += 58f }
    }
}

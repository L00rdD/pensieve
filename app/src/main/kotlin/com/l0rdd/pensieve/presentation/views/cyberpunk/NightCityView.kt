package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.random.Random

/**
 * Night City behind the menus: a grid running to the horizon, falling columns of data, a neon sign.
 * What does not move (the sky, the horizon's glow, the grid's rays, each column of data, the sign
 * and its glow) is drawn once into bitmaps; a frame only moves them.
 */
class NightCityView(context: Context, private val face: Typeface, private val sign: String, clock: FrameClock) : View(context) {
    private val grid = Paint().apply { color = CyberColors.alpha(CyberColors.RED, 0.15f); strokeWidth = 1f }
    private val blit = Paint(Paint.FILTER_BITMAP_FLAG)
    private val random = Random(3)
    private val columns = List(COLUMNS) { floatArrayOf(random.nextFloat() * 1920, random.nextFloat() * 1080, 40 + random.nextFloat() * 80) }
    private var sky: Bitmap? = null
    private var neon: Bitmap? = null
    private var strips: List<Bitmap>? = null
    private var t = 0f

    init {
        clock.add { time, dt ->
            t = time
            columns.forEach { c -> c[1] += c[2] * dt; if (c[1] > 1080 + STRIP) { c[1] = -60f; c[0] = random.nextFloat() * 1920 } }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width; val h = height
        canvas.drawBitmap(sky ?: sky(w, h).also { sky = it }, 0f, 0f, null)
        val horizon = HORIZON
        val off = (t * 40) % 60
        for (z in 0 until 12) { val y = horizon + ((z * 60 + off) / 720f).let { it * it } * 290; canvas.drawLine(0f, y, w.toFloat(), y, grid) }
        val all = strips ?: strips().also { strips = it }
        columns.forEachIndexed { i, c -> canvas.drawBitmap(all[i % all.size], c[0], c[1] - STRIP, null) }
        val cycle = t % 6f
        blit.alpha = if (cycle in 1.1f..1.3f || cycle in 3.7f..3.8f) 50 else 190
        val sign = neon ?: neon().also { neon = it }
        canvas.drawBitmap(sign, w - 70f - sign.width / 2f, 140f, blit)
    }

    /** The sky, the horizon's glow and the grid's rays. */
    private fun sky(w: Int, h: Int): Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
        val c = Canvas(bmp); val p = Paint()
        c.drawColor(CyberColors.BG)
        p.shader = RadialGradient(w * 0.68f, h * 0.32f, w * 0.6f, 0xFF1E0A12.toInt(), CyberColors.BG, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        p.shader = LinearGradient(0f, HORIZON - 140, 0f, HORIZON, 0, CyberColors.alpha(CyberColors.RED, 0.14f), Shader.TileMode.CLAMP)
        c.drawRect(0f, HORIZON - 140, w.toFloat(), HORIZON, p)
        for (i in -26..26) c.drawLine(960f + i * 28, HORIZON, 960f + i * 170, h.toFloat(), grid)
    }

    /** A few columns of katakana, fading up from a bright head, each drawn once. */
    private fun strips(): List<Bitmap> {
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 18f }
        val r = Random(7)
        return List(8) {
            val len = 6 + r.nextInt(12)
            Bitmap.createBitmap(22, STRIP.toInt(), Bitmap.Config.ARGB_8888).also { bmp ->
                val c = Canvas(bmp)
                for (k in 0 until len) {
                    text.color = if (k == 0) CyberColors.alpha(0xFFC8FFFF.toInt(), 0.3f) else CyberColors.alpha(CyberColors.CYAN, 0.12f * (1 - k / len.toFloat()))
                    c.drawText((0x30A0 + r.nextInt(90)).toChar().toString(), 0f, STRIP - 4 - k * 22, text)
                }
            }
        }
    }

    /** The sign down the right edge, with its glow. */
    private fun neon(): Bitmap {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 44f; color = CyberColors.RED; setShadowLayer(18f, 0f, 0f, 0xFFFF2A1F.toInt()); textAlign = Paint.Align.CENTER }
        return Bitmap.createBitmap(100, sign.length * 58 + 60, Bitmap.Config.ARGB_8888).also { bmp ->
            val c = Canvas(bmp); var y = 60f
            sign.forEach { ch -> c.drawText(ch.toString(), 50f, y, p); y += 58f }
        }
    }

    private companion object {
        const val COLUMNS = 30
        const val STRIP = 420f
        const val HORIZON = 790f
    }
}

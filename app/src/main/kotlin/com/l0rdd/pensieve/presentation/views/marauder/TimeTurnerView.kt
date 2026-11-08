package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Hermione's Time-Turner on its chain, at the width it is given, turning on the shared frame clock. */
class TimeTurnerView(context: Context, face: Typeface, inscription: String, clock: FrameClock) : View(context) {
    private val turner = TimeTurnerDrawing(face, inscription)
    private var t = 0f

    init { clock.add { time, _ -> t = time; invalidate() } }

    fun show(now: LocalDateTime) = turner.show(now)

    override fun onDraw(canvas: Canvas) {
        val s = width / 400f
        canvas.save(); canvas.scale(s, s)
        turner.draw(canvas, t, chain = true)
        canvas.restore()
    }
}

/**
 * Hermione's Time-Turner, drawn in a 400 × 420 box with its ring centred on (200, 236): on its
 * chain if asked, the outer ring engraved with its inscription and turning slowly, the hours round
 * it (the current one in red ink), the inner rings turning on themselves, and the hourglass in the
 * middle emptying as the hour goes by.
 */
class TimeTurnerDrawing(face: Typeface, private val inscription: String) {
    private val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFF6B4512.toInt() }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engrave = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 15.5f; letterSpacing = 0.1f; color = 0xFF4A2C0C.toInt() }
    private val hours = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textAlign = Paint.Align.CENTER }
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var outer: Bitmap? = null
    private val goldShader = LinearGradient(0f, 60f, 400f, 420f, intArrayOf(0xFFFFE7A3.toInt(), 0xFFB8862F.toInt(), 0xFFF6D27A.toInt(), 0xFF8F6220.toInt(), 0xFFE8B95A.toInt()), null, Shader.TileMode.CLAMP)
    private val glowShader = RadialGradient(CX, CY, 178f, InkColors.alpha(0xFFFFD27A.toInt(), 0.5f), 0, Shader.TileMode.CLAMP)
    private val glass = Path().apply { moveTo(167f, 159f); lineTo(233f, 159f); lineTo(204f, 234f); lineTo(204f, 238f); lineTo(233f, 313f); lineTo(167f, 313f); lineTo(196f, 238f); lineTo(196f, 234f); close() }
    private var hour = 0
    private var progress = 0f

    fun show(now: LocalDateTime) {
        hour = now.hour % 12
        progress = (now.minute * 60 + now.second) / 3600f
    }

    /** The whole Time-Turner at [t] seconds, [chain] or not. */
    fun draw(canvas: Canvas, t: Float, chain: Boolean) {
        gold.shader = goldShader
        if (chain) drawChain(canvas)
        drawRing(canvas, t)
        drawHours(canvas)
        drawInnerRings(canvas, t)
        drawHourglass(canvas)
        drawSand(canvas, t)
    }

    private fun drawChain(canvas: Canvas) {
        gold.strokeWidth = 3f
        for (k in 0 until 6) {
            val w = if (k % 2 == 1) 3f else 6f
            canvas.drawOval(CX - w, 1f + k * 11f, CX + w, 15f + k * 11f, gold)
        }
    }

    /** The glow, and the outer ring, engraved, turning (drawn once into a bitmap). */
    private fun drawRing(canvas: Canvas, t: Float) {
        fill.shader = glowShader
        canvas.drawCircle(CX, CY, 178f, fill); fill.shader = null
        val ringBitmap = outer ?: engravedRing().also { outer = it }
        canvas.save(); canvas.rotate(t * 4f, CX, CY); canvas.drawBitmap(ringBitmap, CX - 182f, CY - 182f, bitmapPaint); canvas.restore()
    }

    private fun drawHours(canvas: Canvas) {
        fill.color = InkColors.alpha(0xFF46280A.toInt(), 0.08f); canvas.drawCircle(CX, CY, 146f, fill)
        line.strokeWidth = 1f; canvas.drawCircle(CX, CY, 146f, line)
        ROMAN.forEachIndexed { i, r ->
            val a = i * Math.PI / 6
            val now = i == hour
            hours.color = if (now) InkColors.RED else 0xFF4A2C0C.toInt(); hours.textSize = if (now) 21f else 17f
            canvas.drawText(r, CX + sin(a).toFloat() * 130f, CY + 6f - cos(a).toFloat() * 130f, hours)
        }
    }

    private fun drawInnerRings(canvas: Canvas, t: Float) {
        listOf(Triple(112f, 7f, 9f), Triple(96f, -11f, 6f)).forEach { (radius, period, width) ->
            canvas.save(); canvas.scale(cos(t / period * 2 * Math.PI).toFloat(), 1f, CX, CY)
            gold.strokeWidth = width; canvas.drawCircle(CX, CY, radius, gold)
            canvas.restore()
        }
    }

    private fun drawHourglass(canvas: Canvas) {
        fill.shader = goldShader
        canvas.drawRoundRect(160f, 150f, 240f, 159f, 3f, 3f, fill); canvas.drawRoundRect(160f, 313f, 240f, 322f, 3f, 3f, fill)
        fill.shader = null
        fill.color = InkColors.alpha(0xFFFFF6DD.toInt(), 0.3f); canvas.drawPath(glass, fill)
        line.color = 0xFF8F6220.toInt(); line.strokeWidth = 1.4f; canvas.drawPath(glass, line); line.color = 0xFF6B4512.toInt()
    }

    /** The sand left above, the pile below, the grains falling between, and the glass's posts. */
    private fun drawSand(canvas: Canvas, t: Float) {
        val top = 70f * (1 - progress); val hw = 33f * top / 75f
        fill.color = 0xFFD9A441.toInt()
        if (top > 1) canvas.drawPath(Path().apply { moveTo(200 - hw, 234 - top); lineTo(200 + hw, 234 - top); lineTo(201f, 234f); lineTo(199f, 234f); close() }, fill)
        val pile = 64f * sqrt(progress); val pw = 33f * (1 - pile / 75f)
        fill.color = 0xFFC98F2E.toInt()
        canvas.drawPath(Path().apply { moveTo(167f, 313f); lineTo(233f, 313f); lineTo(200 + pw, 313 - pile); quadTo(200f, 313 - pile - 6, 200 - pw, 313 - pile); close() }, fill)
        fill.color = 0xFFD9A441.toInt()
        var y = 236f + (t * 10 % 6); while (y < 313 - pile - 4) { canvas.drawRect(199.2f, y, 200.8f, y + 3, fill); y += 6f }
        gold.strokeWidth = 3f; canvas.drawLine(163f, 159f, 163f, 313f, gold); canvas.drawLine(237f, 159f, 237f, 313f, gold)
    }

    /** The outer ring with its inscription, 364 × 364 around its centre. */
    private fun engravedRing(): Bitmap {
        val bmp = Bitmap.createBitmap(364, 364, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            translate(182f - CX, 182f - CY)
            gold.strokeWidth = 26f; drawCircle(CX, CY, 166f, gold)
            line.strokeWidth = 1.2f; drawCircle(CX, CY, 179f, line); drawCircle(CX, CY, 153f, line)
            drawTextOnPath(inscription, Path().apply { addCircle(CX, CY, 160f, Path.Direction.CW) }, 0f, 0f, engrave)
        }
        return bmp
    }

    companion object {
        /** The centre of the ring, in the drawing's box. */
        const val CX = 200f
        const val CY = 236f
        private val ROMAN = listOf("XII", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI")
    }
}

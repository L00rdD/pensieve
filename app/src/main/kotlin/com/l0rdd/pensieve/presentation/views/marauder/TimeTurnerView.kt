package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
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

/**
 * Hermione's Time-Turner: on its chain, the outer ring engraved with its inscription and turning
 * slowly, the hours round it (the current one in red ink), the inner rings turning on themselves,
 * and the hourglass in the middle emptying as the hour goes by.
 */
class TimeTurnerView(context: Context, private val face: Typeface, private val inscription: String, clock: FrameClock) : View(context) {
    private val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFF6B4512.toInt() }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engrave = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 15.5f; letterSpacing = 0.1f; color = 0xFF4A2C0C.toInt() }
    private val hours = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textAlign = Paint.Align.CENTER }
    private val ring = Path()
    private var t = 0f
    private var hour = 0; private var progress = 0f

    init { clock.add { time, _ -> t = time; invalidate() } }

    fun show(now: LocalDateTime) {
        hour = now.hour % 12
        progress = (now.minute * 60 + now.second) / 3600f
    }

    override fun onDraw(canvas: Canvas) {
        val s = width / 400f
        canvas.save(); canvas.scale(s, s)
        val cx = 200f; val cy = 236f
        gold.shader = LinearGradient(0f, 60f, 400f, 420f, intArrayOf(0xFFFFE7A3.toInt(), 0xFFB8862F.toInt(), 0xFFF6D27A.toInt(), 0xFF8F6220.toInt(), 0xFFE8B95A.toInt()), null, Shader.TileMode.CLAMP)
        // the chain
        gold.strokeWidth = 3f
        for (k in 0 until 6) canvas.drawOval(200f - (if (k % 2 == 1) 3f else 6f), 1f + k * 11f, 200f + (if (k % 2 == 1) 3f else 6f), 15f + k * 11f, gold)
        fill.shader = RadialGradient(cx, cy, 178f, InkColors.alpha(0xFFFFD27A.toInt(), 0.5f), 0, Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, 178f, fill); fill.shader = null
        // the outer ring, engraved, turning
        canvas.save(); canvas.rotate(t * 4f, cx, cy)
        gold.strokeWidth = 26f; canvas.drawCircle(cx, cy, 166f, gold)
        line.strokeWidth = 1.2f; canvas.drawCircle(cx, cy, 179f, line); canvas.drawCircle(cx, cy, 153f, line)
        ring.reset(); ring.addCircle(cx, cy, 160f, Path.Direction.CW)
        canvas.drawTextOnPath(inscription, ring, 0f, 0f, engrave)
        canvas.restore()
        // the hours
        fill.color = InkColors.alpha(0xFF46280A.toInt(), 0.08f); canvas.drawCircle(cx, cy, 146f, fill)
        line.strokeWidth = 1f; canvas.drawCircle(cx, cy, 146f, line)
        ROMAN.forEachIndexed { i, r ->
            val a = i * Math.PI / 6
            hours.color = if (i == hour) InkColors.RED else 0xFF4A2C0C.toInt(); hours.textSize = if (i == hour) 21f else 17f
            canvas.drawText(r, cx + sin(a).toFloat() * 130f, cy + 6f - cos(a).toFloat() * 130f, hours)
        }
        // the inner rings, turning on themselves
        listOf(112f to 7f, 96f to -11f).forEach { (radius, period) ->
            canvas.save(); canvas.scale(cos(t / period * 2 * Math.PI).toFloat(), 1f, cx, cy)
            gold.strokeWidth = if (radius > 100) 9f else 6f; canvas.drawCircle(cx, cy, radius, gold)
            canvas.restore()
        }
        // the hourglass
        fill.shader = gold.shader
        canvas.drawRoundRect(160f, 150f, 240f, 159f, 3f, 3f, fill); canvas.drawRoundRect(160f, 313f, 240f, 322f, 3f, 3f, fill)
        fill.shader = null
        val glass = Path().apply { moveTo(167f, 159f); lineTo(233f, 159f); lineTo(204f, 234f); lineTo(204f, 238f); lineTo(233f, 313f); lineTo(167f, 313f); lineTo(196f, 238f); lineTo(196f, 234f); close() }
        fill.color = InkColors.alpha(0xFFFFF6DD.toInt(), 0.3f); canvas.drawPath(glass, fill)
        line.color = 0xFF8F6220.toInt(); line.strokeWidth = 1.4f; canvas.drawPath(glass, line); line.color = 0xFF6B4512.toInt()
        val top = 70f * (1 - progress); val hw = 33f * top / 75f
        fill.color = 0xFFD9A441.toInt()
        if (top > 1) canvas.drawPath(Path().apply { moveTo(200 - hw, 234 - top); lineTo(200 + hw, 234 - top); lineTo(201f, 234f); lineTo(199f, 234f); close() }, fill)
        val pile = 64f * sqrt(progress); val pw = 33f * (1 - pile / 75f)
        fill.color = 0xFFC98F2E.toInt()
        canvas.drawPath(Path().apply { moveTo(167f, 313f); lineTo(233f, 313f); lineTo(200 + pw, 313 - pile); quadTo(200f, 313 - pile - 6, 200 - pw, 313 - pile); close() }, fill)
        fill.color = 0xFFD9A441.toInt()
        var y = 236f + (t * 10 % 6); while (y < 313 - pile - 4) { canvas.drawRect(199.2f, y, 200.8f, y + 3, fill); y += 6f }
        gold.strokeWidth = 3f; canvas.drawLine(163f, 159f, 163f, 313f, gold); canvas.drawLine(237f, 159f, 237f, 313f, gold)
        canvas.restore()
    }

    private companion object {
        val ROMAN = listOf("XII", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI")
    }
}

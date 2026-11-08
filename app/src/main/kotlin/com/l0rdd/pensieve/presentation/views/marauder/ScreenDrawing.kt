package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.math.min
import kotlin.math.sin

/**
 * The screen drawn as it is, [W] × [H]: its gold roller, the sheet down as far as it is with its hem
 * and pull ring, dashed where it would be open, the red line of where it is going, swaying on its
 * way; "✦ séance" comes out on the sheet past half way, the % under the hem.
 */
class ScreenDrawing(context: Context, private val fell: Typeface, private val fellItalic: Typeface, private val clock: FrameClock, private val seance: String) : View(context) {
    private var screen: Screen? = null
    private var sway = 0f
    private val tick: (Float, Float) -> Unit = { t, _ -> swayAt(t) }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val words = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f; textAlign = Paint.Align.CENTER }
    private val dashed = DashPathEffect(floatArrayOf(3f, 4f), 0f)
    private val aimed = DashPathEffect(floatArrayOf(5f, 4f), 0f)
    private val sheet = LinearGradient(14f, 0f, 90f, 0f, intArrayOf(0xFFE9CF98.toInt(), 0xFFFBF1D8.toInt(), 0xFFFBF1D8.toInt(), 0xFFE2C287.toInt()), floatArrayOf(0f, .15f, .85f, 1f), Shader.TileMode.CLAMP)
    private val gold = LinearGradient(4f, 0f, 100f, 24f, intArrayOf(0xFFFFE7A3.toInt(), 0xFFB8862F.toInt(), 0xFFF6D27A.toInt(), 0xFF8F6220.toInt(), 0xFFE8B95A.toInt()), null, Shader.TileMode.CLAMP)
    private val path = Path()

    /** Shows [screen] where it is. */
    fun show(screen: Screen?) {
        if (screen == this.screen) return
        this.screen = screen
        if (screen?.moving == null) sway = 0f
        invalidate()
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); clock.add(tick) }

    override fun onDetachedFromWindow() { clock.remove(tick); super.onDetachedFromWindow() }

    /** On its way, the sheet sways a little. */
    private fun swayAt(seconds: Float) {
        if (screen?.moving == null) return
        sway = sin(seconds * 7) * 1.6f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val s = screen ?: return
        val p = s.position / 100f
        val end = TOP + LENGTH * p
        drawOutline(canvas)
        if (p > 0) drawSheet(canvas, p, end)
        drawHem(canvas, end)
        drawRoller(canvas)
        s.target?.takeIf { s.moving != null }?.let { drawTarget(canvas, TOP + LENGTH * it / 100f) }
        drawPercent(canvas, s, end)
    }

    /** Where the sheet would be, open. */
    private fun drawOutline(canvas: Canvas) {
        canvas.drawRect(14.5f, TOP + .5f, 89.5f, TOP + LENGTH, stroke(InkColors.alpha(InkColors.INK, .5f), 1f, dashed))
    }

    /** The parchment sheet down to [end], and "✦ séance" on it once it is more than half way down. */
    private fun drawSheet(canvas: Canvas, p: Float, end: Float) {
        path.rewind(); path.moveTo(14f, TOP); path.lineTo(90f, TOP); path.lineTo(90 + sway, end); path.lineTo(14 + sway, end); path.close()
        fill.shader = sheet; canvas.drawPath(path, fill); fill.shader = null
        canvas.drawPath(path, stroke(InkColors.NEWS_INK, 1f))
        if (p <= .55f) return
        words.typeface = fellItalic; words.color = InkColors.alpha(InkColors.NEWS_INK, min(1f, (p - .55f) * 3) * .5f)
        canvas.drawText("✦", 52 + sway / 2, TOP + LENGTH * .42f, words)
        canvas.drawText(seance, 52 + sway / 2, TOP + LENGTH * .42f + 20, words)
    }

    /** The hem bar and its pull ring. */
    private fun drawHem(canvas: Canvas, end: Float) {
        fill.color = InkColors.NEWS_INK
        canvas.drawRect(11 + sway, end - 1, 93 + sway, end + 4, fill)
        stroke(InkColors.NEWS_INK, 1f)
        canvas.drawLine(52 + sway, end + 4, 52 + sway, end + 11, line)
        canvas.drawCircle(52 + sway, end + 14, 3f, line)
    }

    /** The gold roller and its end caps. */
    private fun drawRoller(canvas: Canvas) {
        fill.shader = gold; canvas.drawRoundRect(4f, 8f, 100f, 24f, 7f, 7f, fill); fill.shader = null
        canvas.drawRoundRect(4f, 8f, 100f, 24f, 7f, 7f, stroke(BRONZE, 1f))
        fill.color = BRONZE
        canvas.drawRect(2f, 10f, 6f, 22f, fill); canvas.drawRect(98f, 10f, 102f, 22f, fill)
    }

    /** Where it is going: a red dashed line at [y], with its little arrow. */
    private fun drawTarget(canvas: Canvas, y: Float) {
        canvas.drawLine(4f, y, 100f, y, stroke(InkColors.RED, 2f, aimed))
        path.rewind(); path.moveTo(0f, y - 5); path.lineTo(6f, y); path.lineTo(0f, y + 5); path.close()
        fill.color = InkColors.RED; canvas.drawPath(path, fill)
    }

    private fun drawPercent(canvas: Canvas, s: Screen, end: Float) {
        words.typeface = fell; words.color = if (s.moving != null) InkColors.RED else InkColors.MUTED
        canvas.drawText("${s.position} %", 52f, min(H - 3f, end + 32), words)
    }

    /** The line paint, in [color], [width] wide, plain or dashed by [effect]. */
    private fun stroke(color: Int, width: Float, effect: DashPathEffect? = null): Paint = line.apply {
        this.color = color; strokeWidth = width; pathEffect = effect
    }

    companion object {
        const val W = 104
        const val H = 214
        private const val TOP = 24f
        private const val LENGTH = 172f
        private const val BRONZE = 0xFF6B4512.toInt()
    }
}

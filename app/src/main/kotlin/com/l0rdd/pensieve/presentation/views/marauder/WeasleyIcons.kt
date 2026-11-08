package com.l0rdd.pensieve.presentation.views.marauder

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

/**
 * The engravings of the house's things, drawn in a 30 × 30 box round their centre, each telling
 * its state: the clapperboard shut during the show, the terrarium under its grow light, the torii
 * lit, the hoop in neon, the screen's sheet as far down as it is, footsteps at the desk or walking off.
 */
class WeasleyIcons {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val engravings: Map<Glyph, (Canvas, Icon) -> Unit> = mapOf(
        Glyph.CINEMA to { c, i -> clapperboard(c, i.lit) },
        Glyph.TERRARIUM to { c, i -> terrarium(c, i.lit) },
        Glyph.TORII to { c, i -> torii(c, i.lit) },
        Glyph.HOOP to { c, i -> hoop(c, i.lit) },
        Glyph.PLUG to { c, i -> plug(c, i.lit) },
        Glyph.SCREEN to { c, i -> screen(c, i.level) },
        Glyph.FOOTSTEPS to { c, i -> footsteps(c, i.lit) },
    )

    /** [icon] centred on the canvas's origin, [k] times its size, in [ink]. */
    fun draw(canvas: Canvas, icon: Icon, k: Float, ink: Int) {
        canvas.save(); canvas.scale(k, k)
        stroke.color = ink; stroke.strokeWidth = 1.7f; fill.color = ink
        engravings.getValue(icon.glyph)(canvas, icon)
        canvas.restore()
    }

    /** A clapperboard, open in the interval, shut and filled during the show. */
    private fun clapperboard(canvas: Canvas, show: Boolean) {
        canvas.drawRect(-11f, -2f, 11f, 11f, stroke)
        canvas.save(); canvas.translate(-11f, -3f); canvas.rotate(if (show) 0f else -22f)
        canvas.drawRect(0f, -5f, 22f, 0f, stroke)
        for (i in 0 until 4) canvas.drawLine(3 + i * 5.5f, -5f, 6 + i * 5.5f, 0f, stroke)
        canvas.restore()
        if (show) faint(0.35f) { canvas.drawRect(-11f, -2f, 11f, 11f, fill) }
    }

    /** A jar and its plant, under its grow light when on. */
    private fun terrarium(canvas: Canvas, on: Boolean) {
        canvas.drawRoundRect(-10f, -6f, 10f, 11f, 4f, 4f, stroke)
        canvas.drawLine(-12f, -8f, 12f, -8f, stroke)
        canvas.drawLine(0f, 9f, 0f, -1f, stroke)
        val leaf = if (on) fill else stroke
        leaf(canvas, -4f, 2f, -0.5f, leaf); leaf(canvas, 4f, -1f, 0.5f, leaf)
        if (on) rays(canvas, -6f, 7f)
    }

    private fun leaf(canvas: Canvas, x: Float, y: Float, radians: Float, paint: Paint) {
        canvas.save(); canvas.translate(x, y); canvas.rotate(Math.toDegrees(radians.toDouble()).toFloat())
        canvas.drawOval(-4f, -2f, 4f, 2f, paint); canvas.restore()
    }

    /** The torii gate, lit between its posts when on. */
    private fun torii(canvas: Canvas, on: Boolean) {
        path.reset(); path.moveTo(-15f, -10f); path.quadTo(0f, -6f, 15f, -10f); path.lineTo(14f, -6f); path.quadTo(0f, -3f, -14f, -6f); path.close()
        if (on) canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
        canvas.drawLines(floatArrayOf(-11f, -1f, 11f, -1f, -8f, -5f, -9f, 12f, 8f, -5f, 9f, 12f, 0f, -5f, 0f, -1f), stroke)
        if (on) faint(0.35f) { canvas.drawRect(-7f, 0f, 7f, 11f, fill) }
    }

    /** A basketball hoop and its ball, in neon when on. */
    private fun hoop(canvas: Canvas, on: Boolean) {
        stroke.strokeWidth = if (on) 2.2f else 1.4f
        canvas.drawRect(-10f, -13f, 10f, -2f, stroke)
        canvas.drawOval(-7f, -3.2f, 7f, 1.2f, stroke)
        path.reset(); path.moveTo(-6f, 0f); path.lineTo(-4f, 7f); path.lineTo(4f, 7f); path.lineTo(6f, 0f)
        canvas.drawPath(path, stroke)
        canvas.drawLines(floatArrayOf(-2f, 1f, -1f, 7f, 2f, 1f, 1f, 7f), stroke)
        stroke.strokeWidth = 1.4f
        if (on) canvas.drawCircle(9f, 9f, 4f, fill)
        canvas.drawCircle(9f, 9f, 4f, stroke)
        if (on) canvas.drawLines(floatArrayOf(-13f, -16f, -15f, -18f, 13f, -16f, 15f, -18f, 0f, -16f, 0f, -19f), stroke)
    }

    /** A plug, for an outlet the clock has no engraving of, filled and shining when on. */
    private fun plug(canvas: Canvas, on: Boolean) {
        if (on) canvas.drawRoundRect(-8f, -3f, 8f, 9f, 3f, 3f, fill)
        canvas.drawRoundRect(-8f, -3f, 8f, 9f, 3f, 3f, stroke)
        canvas.drawLines(floatArrayOf(-4f, -3f, -4f, -9f, 4f, -3f, 4f, -9f, 0f, 9f, 0f, 14f), stroke)
        if (on) rays(canvas, -4f, 9f)
    }

    /** The screen: its roller, the sheet down as far as it is ([level] 0..1), and its hem bar. */
    private fun screen(canvas: Canvas, level: Float) {
        canvas.drawRoundRect(-13f, -12f, 13f, -8f, 2f, 2f, fill)
        canvas.drawLines(floatArrayOf(-15f, -10f, -13f, -10f, 13f, -10f, 15f, -10f), stroke)
        val end = -8f + 21f * level
        if (level > 0) {
            faint(0.3f) { canvas.drawRect(-11f, -8f, 11f, end, fill) }
            canvas.drawRect(-11f, -8f, 11f, end, stroke)
        }
        stroke.strokeWidth = 2.4f; canvas.drawLine(-5f, end, 5f, end, stroke)
    }

    /** The Marauder's footsteps: two at the desk, or three walking off, fading. */
    private fun footsteps(canvas: Canvas, here: Boolean) {
        if (!here) { foot(canvas, -8f, 9f, 0.5f); foot(canvas, 0f, 3f, 0.75f); foot(canvas, 8f, -3f, 1f); return }
        canvas.drawRect(-13f, -12f, 13f, -9f, fill); canvas.drawRect(-11f, -9f, -9f, -1f, fill); canvas.drawRect(9f, -9f, 11f, -1f, fill)
        standing(canvas, -4f, 8f); standing(canvas, 4f, 6f)
    }

    private fun standing(canvas: Canvas, x: Float, y: Float) {
        canvas.save(); canvas.translate(x, y); sole(canvas); canvas.restore()
    }

    /** A foot walking off to the top right, at [alpha]. */
    private fun foot(canvas: Canvas, x: Float, y: Float, alpha: Float) {
        canvas.save(); canvas.translate(x, y); canvas.rotate(STEP_DEGREES)
        faint(alpha) { sole(canvas) }
        canvas.restore()
    }

    private fun sole(canvas: Canvas) {
        canvas.drawOval(-3.2f, -5.5f, 3.2f, 5.5f, fill)
        canvas.drawOval(-2.6f, -10.4f, 2.6f, -5.6f, fill)
    }

    /** Seven short rays fanned over (0, [cy]), [r] from it. */
    private fun rays(canvas: Canvas, cy: Float, r: Float) {
        for (i in 0 until 7) {
            val a = Math.toRadians(-90.0 + (i - 3) * 30)
            val c = cos(a).toFloat(); val s = sin(a).toFloat()
            canvas.drawLine(c * r, cy + s * r, c * (r + 4), cy + s * (r + 4), stroke)
        }
    }

    /** Draws [what] with the fill at [alpha] of its ink. */
    private inline fun faint(alpha: Float, what: () -> Unit) {
        val ink = fill.color
        fill.color = InkColors.alpha(ink, alpha)
        what()
        fill.color = ink
    }

    private companion object {
        /** 0.9 rad: the way the footsteps walk off. */
        const val STEP_DEGREES = 51.6f
    }
}

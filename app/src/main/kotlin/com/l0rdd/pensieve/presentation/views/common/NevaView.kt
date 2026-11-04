package com.l0rdd.pensieve.presentation.views.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.Neva.Companion.HEART
import com.l0rdd.pensieve.presentation.views.common.Neva.Companion.PI
import com.l0rdd.pensieve.presentation.views.common.Neva.Companion.TOP
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Neva, the house's seal-point cat, on every look: a few seconds after each return to the home
 * screen, and every minute or two while it stays in front, she crosses the bottom of the screen
 * doing one of her things: a stroll with a meow and maybe a nap and a stretch, a laser dot she
 * stalks, pounces on and chases away, a butterfly she leaps at and misses, or the zoomies.
 * Away, she costs nothing; there, only her own small view is redrawn, and moved by its translation.
 */
class NevaView(context: Context, clock: FrameClock) : View(context) {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leg = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
    private val coat = LinearGradient(0f, -30f, 0f, 30f, BACK, BELLY, Shader.TileMode.CLAMP)
    private val mask = RadialGradient(12f, 6f, 32f, intArrayOf(POINT, POINT and 0xEEFFFFFF.toInt(), POINT and 0x00FFFFFF), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
    private val path = Path()
    private val oval = RectF()

    private val neva = Neva()

    init {
        visibility = INVISIBLE
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        clock.add { time, dt -> frame(time, dt) }
    }

    /** Back on the home screen: we hardly stay there, so she comes at once if she is away. */
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) neva.comeSoon()
    }

    /** Away, she is hidden; there, her view follows her and is drawn again. */
    private fun frame(time: Float, dt: Float) {
        if (!neva.step(time, dt)) {
            if (visibility == VISIBLE) visibility = INVISIBLE
            return
        }
        visibility = VISIBLE
        translationX = neva.x - W / 2f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val now = neva.now
        val said = (now as? Neva.Step.Pause)?.takeIf { it.left > 0.3f }?.say
        val asleep = now is Neva.Step.Nap && neva.lying > 0.7f
        val bob = abs(sin(neva.stride)) * (1.6f + 3f * neva.running) * neva.walking
        val pivot = TOP + 96f + 26f * neva.lying + bob
        val wiggle = if (now is Neva.Step.Stalk && neva.lying > 0.3f) 3f * sin(neva.t * 20f) else 0f

        canvas.save()
        if (neva.dir < 0) canvas.scale(-1f, 1f, W / 2f, 0f)
        canvas.translate(CAT_X + wiggle, -neva.hop)
        canvas.rotate(neva.stretch * 11f, 82f, TOP + 140f)
        canvas.rotate(neva.tilt, 120f, TOP + 90f)
        // far legs, behind everything
        frontLeg(canvas, 152f, pivot, neva.stride + PI, FAR, 12f)
        hindLeg(canvas, 82f, pivot, neva.stride, FAR, 12f)
        tail(canvas, now)
        // the body, darker along the back
        canvas.save(); canvas.translate(120f, TOP + 88f + 22f * neva.lying + bob)
        fill.shader = coat; oval.set(-58f - 4f * neva.lying - 6f * neva.leap, -28f + 4f * neva.lying, 58f + 4f * neva.lying + 6f * neva.leap, 28f); canvas.drawOval(oval, fill); fill.shader = null
        canvas.restore()
        hindLeg(canvas, 82f, pivot, neva.stride + PI, NEAR, 13f)
        fill.color = BACK; fill.alpha = 150; oval.set(66f, pivot - 22f, 102f, pivot + 16f - 10f * neva.lying); canvas.drawOval(oval, fill); fill.alpha = 255
        frontLeg(canvas, 152f, pivot, neva.stride, NEAR, 13f)
        // the Ragdoll's bib
        fill.color = BIB; oval.set(138f, TOP + 60f + 26f * neva.lying + bob, 178f, TOP + 112f + 18f * neva.lying + bob); canvas.drawOval(oval, fill)
        head(canvas, 186f + 4f * neva.lying, TOP + 58f + 38f * neva.lying + bob * 0.6f, asleep, said != null)
        canvas.restore()

        drawPrey(canvas)
        val hx = side(CAT_X + 200f)
        if (said != null) bubble(canvas, said, hx, TOP + 2f - neva.hop)
        if (asleep) snore(canvas, hx, TOP + 40f)
    }

    /** An x of her own drawing, facing right, where it lands on the view whichever way she faces. */
    private fun side(at: Float) = if (neva.dir > 0) at else W - at

    /** A front leg from the shoulder: swinging, stretched forward lying or bowing, reaching in a leap. */
    private fun frontLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val amp = 0.42f + 0.25f * neva.running
        var a = lerp(amp * sin(phase) * neva.walking, 1.4f, neva.lying)
        var l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * neva.walking), 32f, neva.lying)
        a = lerp(a, 1.25f, neva.stretch); l = lerp(l, 44f, neva.stretch)
        a = lerp(a, 1.05f, neva.leap); l = lerp(l, 46f, neva.leap)
        legTo(canvas, px, py, a, l, color, width)
    }

    /** A hind leg from the hip: tucked under her lying, kicked back in a leap. */
    private fun hindLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val amp = 0.42f + 0.25f * neva.running
        var a = amp * sin(phase) * neva.walking * (1f - neva.lying)
        var l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * neva.walking), 6f, neva.lying)
        a = lerp(a, -1.0f, neva.leap); l = lerp(l, 46f, neva.leap)
        legTo(canvas, px, py, a, l, color, width)
    }

    private fun legTo(canvas: Canvas, px: Float, py: Float, a: Float, l: Float, color: Int, width: Float) {
        val fx = px + l * sin(a); val fy = py + l * cos(a)
        leg.color = color; leg.strokeWidth = width
        canvas.drawLine(px, py, fx, fy, leg)
        // her white mitts
        fill.color = MITT; oval.set(fx - 6f, fy - 5f, fx + 10f, fy + 5f); canvas.drawOval(oval, fill)
    }

    /** Up and swaying when she walks, twitching when she stalks, curled round her paws when she naps. */
    private fun tail(canvas: Canvas, now: Neva.Step?) {
        if (now is Neva.Step.Stalk) drawTail(canvas, sin(neva.t * 10f) * 12f, 0f, 0.7f)
        else drawTail(canvas, sin(neva.t * 2.2f) * (8f - 4f * neva.running) * (1f - neva.lying), neva.lying, 0f)
    }

    /** The tail swaying by [sway], curled round her paws by [curl], and laid low by [flat]. */
    private fun drawTail(canvas: Canvas, sway: Float, curl: Float, flat: Float) {
        path.reset()
        path.moveTo(64f, TOP + lerp(84f, 116f, curl))
        path.quadTo(lerp(26f, 40f, curl), TOP + lerp(82f, 142f, curl), lerp(34f + sway, 124f, curl) - 20f * flat, TOP + lerp(32f, 138f, curl) + 50f * flat)
        // bushy, a darker core in a fluffier coat
        leg.color = NEAR; leg.strokeWidth = 24f; canvas.drawPath(path, leg)
        leg.color = POINT; leg.strokeWidth = 14f; canvas.drawPath(path, leg)
    }

    private fun head(canvas: Canvas, cx: Float, cy: Float, asleep: Boolean, meowing: Boolean) {
        canvas.save(); canvas.translate(cx, cy)
        // ears
        fill.color = POINT
        path.reset(); path.moveTo(-22f, -8f); path.lineTo(-14f, -46f); path.lineTo(4f, -20f); path.close(); canvas.drawPath(path, fill)
        path.reset(); path.moveTo(0f, -22f); path.lineTo(18f, -46f); path.lineTo(25f, -6f); path.close(); canvas.drawPath(path, fill)
        fill.color = EAR
        path.reset(); path.moveTo(8f, -22f); path.lineTo(17f, -37f); path.lineTo(20f, -16f); path.close(); canvas.drawPath(path, fill)
        // the cream head, its dark mask and muzzle
        fill.color = BELLY; canvas.drawCircle(0f, 0f, 24f, fill)
        fill.shader = mask; canvas.drawCircle(0f, 0f, 24f, fill); fill.shader = null
        fill.color = POINT; oval.set(8f, -1f, 30f, 17f); canvas.drawOval(oval, fill)
        fill.color = NOSE; oval.set(26f, 0f, 32f, 5f); canvas.drawOval(oval, fill)
        if (meowing) { fill.color = MOUTH; oval.set(19f, 11f, 28f, 19f); canvas.drawOval(oval, fill) }
        // the blue eye, closed asleep or blinking
        val open = if (asleep) 0f else if (neva.blinkIn < 0f) 0.15f else 1f
        if (open < 0.5f) {
            line.color = BELLY; line.strokeWidth = 2f
            oval.set(6f, -8f, 18f, -2f); canvas.drawArc(oval, 10f, 160f, false, line)
        } else {
            fill.color = EYE; oval.set(6.5f, -8.5f, 17.5f, -0.5f); canvas.drawOval(oval, fill)
            fill.color = PUPIL; oval.set(10.5f, -8f, 13.5f, -1f); canvas.drawOval(oval, fill)
            fill.color = MITT; canvas.drawCircle(14.5f, -6f, 1.2f, fill)
        }
        // whiskers
        line.color = WHISKER; line.strokeWidth = 1.2f
        canvas.drawLine(24f, 9f, 50f, 4f, line); canvas.drawLine(24f, 11f, 50f, 12f, line); canvas.drawLine(23f, 13f, 46f, 19f, line)
        canvas.restore()
    }

    /** The red dot on the floor, glowing; the butterfly flapping its wings. */
    private fun drawPrey(canvas: Canvas) {
        val px = neva.preyX - (neva.x - W / 2f)
        when (neva.prey) {
            Neva.Prey.LASER -> {
                val y = TOP + 140f
                fill.color = LASER; fill.alpha = 70; canvas.drawCircle(px, y, 16f, fill)
                fill.alpha = 255; canvas.drawCircle(px, y, 7f, fill)
                fill.color = MITT; canvas.drawCircle(px, y, 2.5f, fill)
            }
            Neva.Prey.BUTTERFLY -> {
                val flap = 0.25f + 0.75f * abs(sin(neva.t * 18f))
                fill.color = WING
                oval.set(px - 26f * flap, neva.preyY - 20f, px, neva.preyY + 3f); canvas.drawOval(oval, fill)
                oval.set(px, neva.preyY - 20f, px + 26f * flap, neva.preyY + 3f); canvas.drawOval(oval, fill)
                fill.color = WING_LOW
                oval.set(px - 18f * flap, neva.preyY - 2f, px, neva.preyY + 16f); canvas.drawOval(oval, fill)
                oval.set(px, neva.preyY - 2f, px + 18f * flap, neva.preyY + 16f); canvas.drawOval(oval, fill)
                line.color = POINT; line.strokeWidth = 3.5f; canvas.drawLine(px, neva.preyY - 13f, px, neva.preyY + 13f, line)
            }
            null -> Unit
        }
    }

    private fun bubble(canvas: Canvas, said: String, cx: Float, top: Float) {
        text.textSize = 22f
        val w = max(34f, text.measureText(said) + 28f)
        oval.set(cx - w / 2f, top, cx + w / 2f, top + 34f)
        fill.color = MITT; canvas.drawRoundRect(oval, 17f, 17f, fill)
        line.color = POINT; line.strokeWidth = 2f; canvas.drawRoundRect(oval, 17f, 17f, line)
        text.color = if (said == HEART) MOUTH else POINT; canvas.drawText(said, cx, top + 24f, text)
    }

    /** Three z rising from her head, each in turn. */
    private fun snore(canvas: Canvas, cx: Float, base: Float) {
        text.color = EYE
        for (i in 0 until 3) {
            val k = ((neva.t / 3f) + i / 3f) % 1f
            text.textSize = 14f + 14f * k
            text.alpha = (255 * sin(k * PI)).toInt()
            canvas.drawText("z", cx + 18f * k, base - 40f * k, text)
        }
        text.alpha = 255
    }

    companion object {
        /** Her view, in stage pixels: her feet on the bottom line, room above her for a meow and ahead of her for her prey. */
        const val W = 600
        const val H = 250
        const val FEET = 1076

        private const val CAT_X = (W - 260) / 2f

        private const val BELLY = 0xFFEEE3D2.toInt()
        private const val BACK = 0xFFCDB394.toInt()
        private const val BIB = 0xFFF5EDE0.toInt()
        private const val POINT = 0xFF3B2920.toInt()
        private const val NEAR = 0xFF5A4233.toInt()
        private const val FAR = 0xFF3E2C23.toInt()
        private const val EAR = 0xFF6E5243.toInt()
        private const val MITT = 0xFFF8F4EE.toInt()
        private const val NOSE = 0xFF1F1511.toInt()
        private const val MOUTH = 0xFFC9636E.toInt()
        private const val EYE = 0xFF8EC3EA.toInt()
        private const val PUPIL = 0xFF0E0B0A.toInt()
        private const val WHISKER = 0xCCFFFFFF.toInt()
        private const val LASER = 0xFFFF2B2B.toInt()
        private const val WING = 0xFFF2A93B.toInt()
        private const val WING_LOW = 0xFFE07A2E.toInt()

        private fun lerp(a: Float, b: Float, k: Float) = a + (b - a) * k
    }
}

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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Neva, the house's seal-point cat, on every look: now and then she walks in along the bottom
 * of the screen, stops, meows, sometimes takes a nap, and walks out the other side. Away, she
 * costs nothing; there, only her own small view is redrawn, and moved by its translation.
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

    private val steps = ArrayDeque<Step>()
    private var away = FIRST_VISIT
    private var x = 0f
    private var dir = 1
    private var stride = 0f       // the walk's phase, in radians
    private var walking = 0f      // 0 standing … 1 walking
    private var lying = 0f        // 0 standing … 1 lying down
    private var blinkIn = 3f
    private var t = 0f

    init {
        visibility = INVISIBLE
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        clock.add { time, dt -> t = time; step(dt) }
    }

    private fun step(dt: Float) {
        val now = steps.firstOrNull()
        if (now == null) {
            if (visibility == VISIBLE) visibility = INVISIBLE
            away -= dt
            if (away <= 0f) plan()
            return
        }
        visibility = VISIBLE
        when (now) {
            is Step.Walk -> if (lying > 0f) { lying = max(0f, lying - dt * 1.2f) } else {
                walking = approach(walking, 1f, dt * 4f)
                x += dir * SPEED * dt * walking
                stride += dt * 7.5f * walking
                if (dir * (now.to - x) <= 0f) steps.removeFirst()
            }
            is Step.Pause -> { walking = approach(walking, 0f, dt * 5f); lying = approach(lying, 0f, dt * 1.2f); now.left -= dt; if (now.left <= 0f) steps.removeFirst() }
            is Step.Nap -> { walking = approach(walking, 0f, dt * 5f); lying = approach(lying, 1f, dt * 0.8f); now.left -= dt; if (now.left <= 0f) steps.removeFirst() }
        }
        blinkIn -= dt
        if (blinkIn < -BLINK) blinkIn = Random.nextFloat() * 3f + 2f
        translationX = x - W / 2f
        invalidate()
    }

    /** Her next visit: in from one side, a stop (a meow, maybe a nap), and out the other. */
    private fun plan() { 
        dir = if (Random.nextBoolean()) 1 else -1
        x = if (dir > 0) -OUT else Stage.WIDTH + OUT
        val stop = Random.nextInt(380, 1540).toFloat()
        steps += Step.Walk(stop)
        steps += Step.Pause(Random.nextFloat() * 2f + 2f, meow = Random.nextFloat() < 0.6f)
        if (Random.nextFloat() < 0.45f) { steps += Step.Nap(Random.nextFloat() * 14f + 12f); steps += Step.Pause(1.5f, meow = false) }
        steps += Step.Walk(if (dir > 0) Stage.WIDTH + OUT else -OUT)
        away = Random.nextFloat() * 300f + 240f
    }

    override fun onDraw(canvas: Canvas) {
        val pause = steps.firstOrNull() as? Step.Pause
        val meowing = pause?.meow == true && pause.left in 0.6f..1.8f
        val asleep = steps.firstOrNull() is Step.Nap && lying > 0.7f
        val bob = abs(sin(stride)) * 1.6f * walking
        val pivot = TOP + 96f + 26f * lying + bob

        canvas.save()
        if (dir < 0) canvas.scale(-1f, 1f, W / 2f, 0f)
        // far legs, behind everything
        frontLeg(canvas, 152f, pivot, stride + PI, FAR, 12f)
        hindLeg(canvas, 82f, pivot, stride, FAR, 12f)
        tail(canvas)
        // the body, darker along the back
        canvas.save(); canvas.translate(120f, TOP + 88f + 22f * lying + bob)
        fill.shader = coat; oval.set(-58f - 4f * lying, -28f + 4f * lying, 58f + 4f * lying, 28f); canvas.drawOval(oval, fill); fill.shader = null
        canvas.restore()
        hindLeg(canvas, 82f, pivot, stride + PI, NEAR, 13f)
        fill.color = BACK; fill.alpha = 150; oval.set(66f, pivot - 22f, 102f, pivot + 16f - 10f * lying); canvas.drawOval(oval, fill); fill.alpha = 255
        frontLeg(canvas, 152f, pivot, stride, NEAR, 13f)
        // the Ragdoll's bib
        fill.color = BIB; oval.set(138f, TOP + 60f + 26f * lying + bob, 178f, TOP + 112f + 18f * lying + bob); canvas.drawOval(oval, fill)
        head(canvas, 186f + 4f * lying, TOP + 58f + 38f * lying + bob * 0.6f, asleep, meowing)
        canvas.restore()

        val hx = if (dir > 0) 200f else W - 200f
        if (meowing) bubble(canvas, hx, TOP + 2f)
        if (asleep) snore(canvas, hx, TOP + 40f)
    }

    /** A front leg from the shoulder, swinging when she walks, stretched forward when she lies. */
    private fun frontLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val swing = 0.42f * sin(phase) * walking
        val a = lerp(swing, 1.4f, lying)
        val l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * walking), 32f, lying)
        legTo(canvas, px, py, a, l, color, width)
    }

    /** A hind leg from the hip, tucked under her when she lies. */
    private fun hindLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val a = 0.42f * sin(phase) * walking * (1f - lying)
        val l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * walking), 6f, lying)
        legTo(canvas, px, py, a, l, color, width)
    }

    private fun legTo(canvas: Canvas, px: Float, py: Float, a: Float, l: Float, color: Int, width: Float) {
        val fx = px + l * sin(a); val fy = py + l * cos(a)
        leg.color = color; leg.strokeWidth = width
        canvas.drawLine(px, py, fx, fy, leg)
        // her white mitts
        fill.color = MITT; oval.set(fx - 6f, fy - 5f, fx + 10f, fy + 5f); canvas.drawOval(oval, fill)
    }

    /** Up and swaying when she walks, curled round her paws when she naps. */
    private fun tail(canvas: Canvas) {
        val sway = sin(t * 2.2f) * 8f * (1f - lying)
        path.reset()
        path.moveTo(64f, TOP + lerp(84f, 116f, lying))
        path.quadTo(lerp(26f, 40f, lying), TOP + lerp(82f, 142f, lying), lerp(34f + sway, 124f, lying), TOP + lerp(32f, 138f, lying))
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
        val open = if (asleep) 0f else if (blinkIn < 0f) 0.15f else 1f
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

    private fun bubble(canvas: Canvas, cx: Float, top: Float) {
        text.textSize = 22f
        val w = text.measureText(MEOW) + 28f
        oval.set(cx - w / 2f, top, cx + w / 2f, top + 34f)
        fill.color = MITT; canvas.drawRoundRect(oval, 17f, 17f, fill)
        line.color = POINT; line.strokeWidth = 2f; canvas.drawRoundRect(oval, 17f, 17f, line)
        text.color = POINT; canvas.drawText(MEOW, cx, top + 24f, text)
    }

    /** Three z rising from her head, each in turn. */
    private fun snore(canvas: Canvas, cx: Float, base: Float) {
        text.color = EYE
        for (i in 0 until 3) {
            val k = ((t / 3f) + i / 3f) % 1f
            text.textSize = 14f + 14f * k
            text.alpha = (255 * sin(k * PI)).toInt()
            canvas.drawText("z", cx + 18f * k, base - 40f * k, text)
        }
        text.alpha = 255
    }

    private sealed interface Step {
        class Walk(val to: Float) : Step
        class Pause(var left: Float, val meow: Boolean) : Step
        class Nap(var left: Float) : Step
    }

    companion object {
        /** Her view, in stage pixels: her feet on the bottom line, room above her for a meow. */
        const val W = 260
        const val H = 190
        const val FEET = 1076

        private const val TOP = 40f
        private const val OUT = 160f
        private const val SPEED = 72f
        private const val BLINK = 0.15f
        private const val FIRST_VISIT = 45f
        private const val PI = Math.PI.toFloat()
        private const val MEOW = "miaou !"

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

        private fun lerp(a: Float, b: Float, k: Float) = a + (b - a) * k
        private fun approach(v: Float, to: Float, by: Float) = if (v < to) min(to, v + by) else max(to, v - by)
    }
}

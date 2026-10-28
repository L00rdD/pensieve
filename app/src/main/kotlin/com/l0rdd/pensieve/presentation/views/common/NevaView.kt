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

    private val steps = ArrayDeque<Step>()
    private var away = soon()
    private var x = 0f            // her middle, in stage pixels
    private var dir = 1
    private var stride = 0f       // the walk's phase, in radians
    private var walking = 0f      // 0 standing … 1 walking
    private var running = 0f      // 0 walking … 1 running flat out
    private var lying = 0f        // 0 standing … 1 lying down
    private var stretch = 0f      // 0 … 1 bowing forward, rear up
    private var leap = 0f         // 0 … 1 legs stretched out in the air
    private var hop = 0f          // her height above the floor
    private var tilt = 0f         // nose up (negative) or down, in degrees
    private var blinkIn = 3f
    private var t = 0f

    private var prey: Prey? = null
    private var preyX = 0f        // in stage pixels
    private var preyY = 0f        // in the view's pixels
    private var preyFled = false

    init {
        visibility = INVISIBLE
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        clock.add { time, dt -> t = time; step(dt) }
    }

    /** Back on the home screen: we hardly stay there, so she comes at once if she is away. */
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE && steps.isEmpty()) away = soon()
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
        val still = now !is Step.Walk
        if (still) walking = approach(walking, 0f, dt * 5f)
        if (now !is Step.Stretch) stretch = approach(stretch, 0f, dt * 2f)
        if (now !is Step.Pounce) { leap = approach(leap, 0f, dt * 4f); hop = 0f; tilt = 0f }
        when (now) {
            is Step.Walk -> if (lying > 0f) { lying = max(0f, lying - dt * 1.5f) } else {
                val fast = if (now.speed > SPEED) 1f else 0f
                running = approach(running, fast, dt * 3f)
                walking = approach(walking, 1f, dt * (4f + 6f * fast))
                x += dir * now.speed * dt * walking
                stride += dt * (7.5f + 8.5f * running) * walking
                if (dir * (now.to - x) <= 0f) done()
            }
            is Step.Pause -> { lying = approach(lying, 0f, dt * 1.2f); now.left -= dt; if (now.left <= 0f) done() }
            is Step.Nap -> { lying = approach(lying, 1f, dt * 0.8f); now.left -= dt; if (now.left <= 0f) done() }
            is Step.Stretch -> { lying = approach(lying, 0f, dt * 1.5f); now.left -= dt; stretch = sin(PI * (1f - now.left / now.length).coerceIn(0f, 1f)); if (now.left <= 0f) done() }
            is Step.Stalk -> { lying = approach(lying, 0.45f, dt * 2f); now.left -= dt; if (now.left <= 0f) done() }
            is Step.Pounce -> {
                if (now.k == 0f) now.from = x
                lying = 0f
                now.k = min(1f, now.k + dt / POUNCE)
                x = now.from + dir * now.length * now.k
                hop = now.height * sin(PI * now.k)
                leap = sin(PI * now.k)
                tilt = -14f * (1f - 2f * now.k) * leap
                if (now.k >= 1f) { done(); preyFled = true }
            }
        }
        movePrey(dt)
        blinkIn -= dt
        if (blinkIn < -BLINK) blinkIn = Random.nextFloat() * 3f + 2f
        translationX = x - W / 2f
        invalidate()
    }

    private fun done() {
        steps.removeFirst()
        if (steps.isEmpty()) prey = null
    }

    /** The laser dot jitters, then runs ahead of her; the butterfly flutters, then flies off. */
    private fun movePrey(dt: Float) {
        when (prey) {
            Prey.LASER -> if (preyFled) preyX = x + dir * 190f + 8f * sin(t * 11f) else preyX += 30f * sin(t * 7f) * dt
            Prey.BUTTERFLY -> if (preyFled) { preyX += dir * 90f * dt; preyY -= 70f * dt; if (preyY < -20f) prey = null }
                else { preyX += 40f * sin(t * 2.3f) * dt; preyY = BUTTERFLY_Y + 12f * sin(t * 3.1f) }
            null -> Unit
        }
    }

    /** Her next visit: in from one side, one of her things, and out the other. */
    private fun plan() {
        dir = if (Random.nextBoolean()) 1 else -1
        x = if (dir > 0) -OUT else Stage.WIDTH + OUT
        val exit = if (dir > 0) Stage.WIDTH + OUT else -OUT
        val stop = Random.nextInt(380, 1300).toFloat().let { if (dir > 0) it else Stage.WIDTH - it }
        prey = null; preyFled = false
        when (Random.nextInt(4)) {
            0 -> {
                steps += Step.Walk(stop)
                steps += Step.Pause(Random.nextFloat() * 2f + 2.5f, SAYINGS.random())
                if (Random.nextBoolean()) { steps += Step.Nap(Random.nextFloat() * 10f + 10f); steps += Step.Stretch(1.8f); steps += Step.Pause(0.8f) }
                steps += Step.Walk(exit)
            }
            1 -> {
                steps += Step.Walk(stop)
                steps += Step.Pause(0.4f)
                steps += Step.Pause(1.2f, "!")
                steps += Step.Stalk(2f)
                steps += Step.Pounce(150f, 34f)
                steps += Step.Walk(exit, RUN)
                prey = Prey.LASER; preyX = stop + dir * 170f
            }
            2 -> {
                steps += Step.Walk(stop)
                steps += Step.Pause(1.6f)
                steps += Step.Stalk(1.2f)
                steps += Step.Pounce(150f, 80f)
                steps += Step.Pause(2.2f, "?")
                steps += Step.Walk(exit)
                prey = Prey.BUTTERFLY; preyX = stop + dir * 200f; preyY = BUTTERFLY_Y
            }
            else -> {
                steps += Step.Walk(stop, RUN)
                steps += Step.Pause(1.6f, "mrrp ?")
                steps += Step.Stretch(1.4f)
                steps += Step.Walk(exit, RUN)
            }
        }
        away = Random.nextFloat() * 120f + 60f
    }

    override fun onDraw(canvas: Canvas) {
        val now = steps.firstOrNull()
        val said = (now as? Step.Pause)?.takeIf { it.left > 0.3f }?.say
        val asleep = now is Step.Nap && lying > 0.7f
        val bob = abs(sin(stride)) * (1.6f + 3f * running) * walking
        val pivot = TOP + 96f + 26f * lying + bob
        val wiggle = if (now is Step.Stalk && lying > 0.3f) 3f * sin(t * 20f) else 0f

        canvas.save()
        if (dir < 0) canvas.scale(-1f, 1f, W / 2f, 0f)
        canvas.translate(CAT_X + wiggle, -hop)
        canvas.rotate(stretch * 11f, 82f, TOP + 140f)
        canvas.rotate(tilt, 120f, TOP + 90f)
        // far legs, behind everything
        frontLeg(canvas, 152f, pivot, stride + PI, FAR, 12f)
        hindLeg(canvas, 82f, pivot, stride, FAR, 12f)
        tail(canvas, now)
        // the body, darker along the back
        canvas.save(); canvas.translate(120f, TOP + 88f + 22f * lying + bob)
        fill.shader = coat; oval.set(-58f - 4f * lying - 6f * leap, -28f + 4f * lying, 58f + 4f * lying + 6f * leap, 28f); canvas.drawOval(oval, fill); fill.shader = null
        canvas.restore()
        hindLeg(canvas, 82f, pivot, stride + PI, NEAR, 13f)
        fill.color = BACK; fill.alpha = 150; oval.set(66f, pivot - 22f, 102f, pivot + 16f - 10f * lying); canvas.drawOval(oval, fill); fill.alpha = 255
        frontLeg(canvas, 152f, pivot, stride, NEAR, 13f)
        // the Ragdoll's bib
        fill.color = BIB; oval.set(138f, TOP + 60f + 26f * lying + bob, 178f, TOP + 112f + 18f * lying + bob); canvas.drawOval(oval, fill)
        head(canvas, 186f + 4f * lying, TOP + 58f + 38f * lying + bob * 0.6f, asleep, said != null)
        canvas.restore()

        drawPrey(canvas)
        val hx = side(CAT_X + 200f)
        if (said != null) bubble(canvas, said, hx, TOP + 2f - hop)
        if (asleep) snore(canvas, hx, TOP + 40f)
    }

    /** An x of her own drawing, facing right, where it lands on the view whichever way she faces. */
    private fun side(at: Float) = if (dir > 0) at else W - at

    /** A front leg from the shoulder: swinging, stretched forward lying or bowing, reaching in a leap. */
    private fun frontLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val amp = 0.42f + 0.25f * running
        var a = lerp(amp * sin(phase) * walking, 1.4f, lying)
        var l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * walking), 32f, lying)
        a = lerp(a, 1.25f, stretch); l = lerp(l, 44f, stretch)
        a = lerp(a, 1.05f, leap); l = lerp(l, 46f, leap)
        legTo(canvas, px, py, a, l, color, width)
    }

    /** A hind leg from the hip: tucked under her lying, kicked back in a leap. */
    private fun hindLeg(canvas: Canvas, px: Float, py: Float, phase: Float, color: Int, width: Float) {
        val amp = 0.42f + 0.25f * running
        var a = amp * sin(phase) * walking * (1f - lying)
        var l = lerp(44f * (1f - 0.12f * max(0f, cos(phase)) * walking), 6f, lying)
        a = lerp(a, -1.0f, leap); l = lerp(l, 46f, leap)
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
    private fun tail(canvas: Canvas, now: Step?) {
        val stalking = now is Step.Stalk
        val sway = sin(t * (if (stalking) 10f else 2.2f)) * (if (stalking) 12f else 8f - 4f * running) * (1f - lying * (if (stalking) 0f else 1f))
        val curl = if (stalking) 0f else lying
        val flat = if (stalking) 0.7f else 0f
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

    /** The red dot on the floor, glowing; the butterfly flapping its wings. */
    private fun drawPrey(canvas: Canvas) {
        val px = preyX - (x - W / 2f)
        when (prey) {
            Prey.LASER -> {
                val y = TOP + 140f
                fill.color = LASER; fill.alpha = 70; canvas.drawCircle(px, y, 16f, fill)
                fill.alpha = 255; canvas.drawCircle(px, y, 7f, fill)
                fill.color = MITT; canvas.drawCircle(px, y, 2.5f, fill)
            }
            Prey.BUTTERFLY -> {
                val flap = 0.25f + 0.75f * abs(sin(t * 18f))
                fill.color = WING
                oval.set(px - 26f * flap, preyY - 20f, px, preyY + 3f); canvas.drawOval(oval, fill)
                oval.set(px, preyY - 20f, px + 26f * flap, preyY + 3f); canvas.drawOval(oval, fill)
                fill.color = WING_LOW
                oval.set(px - 18f * flap, preyY - 2f, px, preyY + 16f); canvas.drawOval(oval, fill)
                oval.set(px, preyY - 2f, px + 18f * flap, preyY + 16f); canvas.drawOval(oval, fill)
                line.color = POINT; line.strokeWidth = 3.5f; canvas.drawLine(px, preyY - 13f, px, preyY + 13f, line)
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
            val k = ((t / 3f) + i / 3f) % 1f
            text.textSize = 14f + 14f * k
            text.alpha = (255 * sin(k * PI)).toInt()
            canvas.drawText("z", cx + 18f * k, base - 40f * k, text)
        }
        text.alpha = 255
    }

    private enum class Prey { LASER, BUTTERFLY }

    private sealed interface Step {
        class Walk(val to: Float, val speed: Float = SPEED) : Step
        class Pause(var left: Float, val say: String? = null) : Step
        class Nap(var left: Float) : Step
        class Stretch(val length: Float) : Step { var left = length }
        class Stalk(var left: Float) : Step
        class Pounce(val length: Float, val height: Float) : Step { var k = 0f; var from = 0f }
    }

    companion object {
        /** Her view, in stage pixels: her feet on the bottom line, room above her for a meow and ahead of her for her prey. */
        const val W = 600
        const val H = 250
        const val FEET = 1076

        private const val CAT_X = (W - 260) / 2f
        private const val TOP = 100f
        private const val OUT = 160f
        private const val SPEED = 72f
        private const val RUN = 340f
        private const val POUNCE = 0.55f
        private const val BUTTERFLY_Y = TOP + 30f
        private const val BLINK = 0.15f
        private const val PI = Math.PI.toFloat()
        private const val HEART = "♥"
        private val SAYINGS = listOf("miaou !", "mrrp ?", HEART, "miaou !")

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

        private fun soon() = Random.nextFloat() * 5f + 3f
        private fun lerp(a: Float, b: Float, k: Float) = a + (b - a) * k
        private fun approach(v: Float, to: Float, by: Float) = if (v < to) min(to, v + by) else max(to, v - by)
    }
}

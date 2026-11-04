package com.l0rdd.pensieve.presentation.views.common

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Neva herself, without a pixel: where she is along the bottom of the stage, what she is doing and
 * how far into it, and her prey. [NevaView] draws her; [random] is only fixed by the tests.
 */
class Neva(private val random: Random = Random) {
    private val steps = ArrayDeque<Step>()
    private var away = soon()
    var x = 0f; private set            // her middle, in stage pixels
    var dir = 1; private set
    var stride = 0f; private set       // the walk's phase, in radians
    var walking = 0f; private set      // 0 standing … 1 walking
    var running = 0f; private set      // 0 walking … 1 running flat out
    var lying = 0f; private set        // 0 standing … 1 lying down
    var stretch = 0f; private set      // 0 … 1 bowing forward, rear up
    var leap = 0f; private set         // 0 … 1 legs stretched out in the air
    var hop = 0f; private set          // her height above the floor
    var tilt = 0f; private set         // nose up (negative) or down, in degrees
    var blinkIn = 3f; private set
    var t = 0f; private set

    var prey: Prey? = null; private set
    var preyX = 0f; private set        // in stage pixels
    var preyY = 0f; private set        // in her view's pixels
    private var preyFled = false

    /** What she is doing, null while she is away. */
    val now: Step? get() = steps.firstOrNull()

    /** Back on the home screen: we hardly stay there, so she comes at once if she is away. */
    fun comeSoon() {
        if (steps.isEmpty()) away = soon()
    }

    /** Moves her on by [dt] seconds, [time] being the clock's; false while she is away. */
    fun step(time: Float, dt: Float): Boolean {
        t = time
        val now = steps.firstOrNull() ?: return awayFor(dt)
        settle(now, dt)
        act(now, dt)
        movePrey(dt)
        blinkIn -= dt
        if (blinkIn < -BLINK) blinkIn = random.nextFloat() * 3f + 2f
        return true
    }

    private fun awayFor(dt: Float): Boolean {
        away -= dt
        if (away <= 0f) plan()
        return false
    }

    /** What she is not doing any more wears off. */
    private fun settle(now: Step, dt: Float) {
        if (now !is Step.Walk) walking = approach(walking, 0f, dt * 5f)
        if (now !is Step.Stretch) stretch = approach(stretch, 0f, dt * 2f)
        if (now !is Step.Pounce) { leap = approach(leap, 0f, dt * 4f); hop = 0f; tilt = 0f }
    }

    private fun act(now: Step, dt: Float) {
        when (now) {
            is Step.Walk -> walk(now, dt)
            is Step.Pause -> { lying = approach(lying, 0f, dt * 1.2f); now.left -= dt; doneAt(now.left) }
            is Step.Nap -> { lying = approach(lying, 1f, dt * 0.8f); now.left -= dt; doneAt(now.left) }
            is Step.Stretch -> stretchOut(now, dt)
            is Step.Stalk -> { lying = approach(lying, 0.45f, dt * 2f); now.left -= dt; doneAt(now.left) }
            is Step.Pounce -> pounce(now, dt)
        }
    }

    /** Up first if she was lying, then walking or running to [Step.Walk.to]. */
    private fun walk(now: Step.Walk, dt: Float) {
        if (lying > 0f) { lying = max(0f, lying - dt * 1.5f); return }
        val fast = if (now.speed > SPEED) 1f else 0f
        running = approach(running, fast, dt * 3f)
        walking = approach(walking, 1f, dt * (4f + 6f * fast))
        x += dir * now.speed * dt * walking
        stride += dt * (7.5f + 8.5f * running) * walking
        if (dir * (now.to - x) <= 0f) done()
    }

    private fun stretchOut(now: Step.Stretch, dt: Float) {
        lying = approach(lying, 0f, dt * 1.5f)
        now.left -= dt
        stretch = sin(PI * (1f - now.left / now.length).coerceIn(0f, 1f))
        doneAt(now.left)
    }

    private fun pounce(now: Step.Pounce, dt: Float) {
        if (now.k == 0f) now.from = x
        lying = 0f
        now.k = min(1f, now.k + dt / POUNCE)
        x = now.from + dir * now.length * now.k
        hop = now.height * sin(PI * now.k)
        leap = sin(PI * now.k)
        tilt = -14f * (1f - 2f * now.k) * leap
        if (now.k >= 1f) { done(); preyFled = true }
    }

    private fun doneAt(left: Float) {
        if (left <= 0f) done()
    }

    private fun done() {
        steps.removeFirst()
        if (steps.isEmpty()) prey = null
    }

    /** The laser dot jitters, then runs ahead of her; the butterfly flutters, then flies off. */
    private fun movePrey(dt: Float) {
        when (prey) {
            Prey.LASER -> moveLaser(dt)
            Prey.BUTTERFLY -> moveButterfly(dt)
            null -> Unit
        }
    }

    private fun moveLaser(dt: Float) {
        if (preyFled) preyX = x + dir * 190f + 8f * sin(t * 11f) else preyX += 30f * sin(t * 7f) * dt
    }

    private fun moveButterfly(dt: Float) {
        if (!preyFled) { preyX += 40f * sin(t * 2.3f) * dt; preyY = BUTTERFLY_Y + 12f * sin(t * 3.1f); return }
        preyX += dir * 90f * dt; preyY -= 70f * dt
        if (preyY < -20f) prey = null
    }

    /** Her next visit: in from one side, one of her things, and out the other. */
    private fun plan() {
        dir = if (random.nextBoolean()) 1 else -1
        x = fromLeft(-OUT, Stage.WIDTH + OUT)
        val exit = fromLeft(Stage.WIDTH + OUT, -OUT)
        val stop = random.nextInt(380, 1300).toFloat().let { fromLeft(it, Stage.WIDTH - it) }
        prey = null; preyFled = false
        when (random.nextInt(4)) {
            0 -> stroll(stop, exit)
            1 -> laser(stop, exit)
            2 -> butterfly(stop, exit)
            else -> zoomies(stop, exit)
        }
        away = random.nextFloat() * 120f + 60f
    }

    /** [left] when she comes from the left, [right] when she comes from the right. */
    private fun fromLeft(left: Float, right: Float) = if (dir > 0) left else right

    /** A stroll with a meow, and maybe a nap and a stretch. */
    private fun stroll(stop: Float, exit: Float) {
        steps += Step.Walk(stop)
        steps += Step.Pause(random.nextFloat() * 2f + 2.5f, SAYINGS.random(random))
        if (random.nextBoolean()) { steps += Step.Nap(random.nextFloat() * 10f + 10f); steps += Step.Stretch(1.8f); steps += Step.Pause(0.8f) }
        steps += Step.Walk(exit)
    }

    /** A laser dot she stalks, pounces on and chases away. */
    private fun laser(stop: Float, exit: Float) {
        steps += Step.Walk(stop)
        steps += Step.Pause(0.4f)
        steps += Step.Pause(1.2f, "!")
        steps += Step.Stalk(2f)
        steps += Step.Pounce(150f, 34f)
        steps += Step.Walk(exit, RUN)
        prey = Prey.LASER; preyX = stop + dir * 170f
    }

    /** A butterfly she leaps at and misses. */
    private fun butterfly(stop: Float, exit: Float) {
        steps += Step.Walk(stop)
        steps += Step.Pause(1.6f)
        steps += Step.Stalk(1.2f)
        steps += Step.Pounce(150f, 80f)
        steps += Step.Pause(2.2f, "?")
        steps += Step.Walk(exit)
        prey = Prey.BUTTERFLY; preyX = stop + dir * 200f; preyY = BUTTERFLY_Y
    }

    private fun zoomies(stop: Float, exit: Float) {
        steps += Step.Walk(stop, RUN)
        steps += Step.Pause(1.6f, "mrrp ?")
        steps += Step.Stretch(1.4f)
        steps += Step.Walk(exit, RUN)
    }

    private fun soon() = random.nextFloat() * 5f + 3f

    enum class Prey { LASER, BUTTERFLY }

    sealed interface Step {
        class Walk(val to: Float, val speed: Float = SPEED) : Step
        class Pause(var left: Float, val say: String? = null) : Step
        class Nap(var left: Float) : Step
        class Stretch(val length: Float) : Step { var left = length }
        class Stalk(var left: Float) : Step
        class Pounce(val length: Float, val height: Float) : Step { var k = 0f; var from = 0f }
    }

    companion object {
        /** The top of her drawing in her view, in its pixels. */
        const val TOP = 100f
        const val HEART = "♥"
        const val PI = Math.PI.toFloat()

        private const val OUT = 160f
        private const val SPEED = 72f
        private const val RUN = 340f
        private const val POUNCE = 0.55f
        private const val BUTTERFLY_Y = TOP + 30f
        private const val BLINK = 0.15f
        private val SAYINGS = listOf("miaou !", "mrrp ?", HEART, "miaou !")

        private fun approach(v: Float, to: Float, by: Float) = if (v < to) min(to, v + by) else max(to, v - by)
    }
}

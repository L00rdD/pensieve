package com.l0rdd.pensieve.presentation.views.common

import kotlin.math.abs
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NevaTest {

    /** A random that answers what it is told, in order; then halfway. */
    private class Scripted(ints: List<Int> = emptyList(), floats: List<Float> = emptyList(), booleans: List<Boolean> = emptyList()) : Random() {
        private val ints = ArrayDeque(ints)
        private val floats = ArrayDeque(floats)
        private val booleans = ArrayDeque(booleans)
        var floatsDrawn = 0

        override fun nextBits(bitCount: Int) = error("not scripted")
        override fun nextInt(until: Int) = ints.removeFirstOrNull() ?: 0
        override fun nextInt(from: Int, until: Int) = ints.removeFirstOrNull() ?: from
        override fun nextFloat(): Float { floatsDrawn++; return floats.removeFirstOrNull() ?: 0.5f }
        override fun nextBoolean() = booleans.removeFirstOrNull() ?: false
    }

    private var time = 0f

    /** Steps her on for [seconds], telling [each] how she is after every step. */
    private fun Neva.run(seconds: Float, each: (there: Boolean) -> Unit = {}) {
        repeat((seconds / DT).toInt()) {
            time += DT
            each(step(time, DT))
        }
    }

    /** Steps her on until [done] holds, and fails if it never does. */
    private fun Neva.until(done: Neva.() -> Boolean) {
        repeat(10_000) {
            time += DT
            step(time, DT)
            if (done()) return
        }
        error("never happened")
    }

    @Test
    fun `away a few seconds, she strolls in from the left, meows at her stop and walks out the other side`() {
        // 3 s away; from the left, stopping at 500, a stroll; a 2.5 s pause saying ♥, no nap
        val neva = Neva(Scripted(ints = listOf(500, 0, 2), floats = listOf(0f, 0f), booleans = listOf(true, false)))
        neva.run(2.9f) { assertFalse(it) }
        assertNull(neva.now)
        neva.until { now != null }
        assertEquals(-160f, neva.x, 1f)
        assertEquals(1, neva.dir)
        assertNull(neva.prey)
        neva.until { now is Neva.Step.Pause }
        assertEquals(500f, neva.x, 3f)
        assertEquals(Neva.HEART, (neva.now as Neva.Step.Pause).say)
        assertTrue(neva.walking > 0f)
        neva.run(2.6f)
        assertTrue(neva.now is Neva.Step.Walk)
        assertEquals(0f, neva.walking, 0.5f)
        var gone = false
        neva.run(40f) { if (!it) gone = true }
        assertTrue(gone)
        assertNull(neva.now)
        assertTrue(neva.x >= Stage.WIDTH + 160f)
        assertEquals(0f, neva.running, 0f)
    }

    @Test
    fun `on a stroll she may nap, lying down, then stretch before going`() {
        val neva = Neva(Scripted(ints = listOf(500, 0, 0), floats = listOf(0f, 0f, 0f), booleans = listOf(true, true)))
        neva.until { now is Neva.Step.Nap }
        neva.run(5f)
        assertEquals(1f, neva.lying, 0f)
        neva.until { now is Neva.Step.Stretch }
        var bowed = 0f
        while (neva.now is Neva.Step.Stretch) neva.run(DT) { bowed = maxOf(bowed, neva.stretch) }
        assertTrue(bowed > 0.95f)
        assertEquals(0f, neva.lying, 0f)
        assertNull((neva.now as Neva.Step.Pause).say)
        neva.run(1f)
        assertEquals(0f, neva.stretch, 0f)
        assertTrue(neva.now is Neva.Step.Walk)
    }

    @Test
    fun `from the right she stalks a laser dot, pounces on it and chases it away`() {
        val neva = Neva(Scripted(ints = listOf(500, 1), floats = listOf(0f), booleans = listOf(false)))
        neva.until { now != null }
        assertEquals(-1, neva.dir)
        assertEquals(Stage.WIDTH + 160f, neva.x, 1f)
        assertEquals(Neva.Prey.LASER, neva.prey)
        assertEquals(Stage.WIDTH - 500f - 170f, neva.preyX, 1f)
        neva.until { now is Neva.Step.Pause }
        assertEquals(Stage.WIDTH - 500f - 170f, neva.preyX, 30f)
        neva.until { (now as? Neva.Step.Pause)?.say == "!" }
        neva.until { now is Neva.Step.Stalk }
        neva.run(1.5f)
        assertEquals(0.45f, neva.lying, 0.01f)
        neva.until { now is Neva.Step.Pounce }
        var highest = 0f; var reached = 0f; var tilted = 0f
        while (neva.now is Neva.Step.Pounce) neva.run(DT) { highest = maxOf(highest, neva.hop); reached = maxOf(reached, neva.leap); tilted = minOf(tilted, neva.tilt) }
        assertEquals(34f, highest, 1f)
        assertTrue(reached > 0.95f && tilted < -4f)
        assertEquals(0f, neva.lying, 0f)
        neva.run(0.5f)
        assertEquals(neva.x - 190f, neva.preyX, 8f)
        assertTrue(neva.running > 0.9f)
        neva.until { now == null }
        assertNull(neva.prey)
        assertEquals(0f, neva.hop, 0f)
    }

    @Test
    fun `she leaps at a butterfly and misses it, and it flies off`() {
        val neva = Neva(Scripted(ints = listOf(500, 2), floats = listOf(0f), booleans = listOf(true)))
        neva.until { now != null }
        assertEquals(Neva.Prey.BUTTERFLY, neva.prey)
        assertEquals(700f, neva.preyX, 1f)
        neva.run(3f)
        assertEquals(Neva.TOP + 30f, neva.preyY, 12f)
        neva.until { now is Neva.Step.Pounce }
        var highest = 0f
        while (neva.now is Neva.Step.Pounce) neva.run(DT) { highest = maxOf(highest, neva.hop) }
        assertEquals(80f, highest, 1f)
        assertEquals("?", (neva.now as Neva.Step.Pause).say)
        val flown = neva.preyX
        neva.run(0.5f)
        assertTrue(neva.preyX > flown)
        neva.until { prey == null }
        assertTrue(neva.now != null)
    }

    @Test
    fun `the zoomies, she runs flat out, says mrrp and stretches`() {
        val neva = Neva(Scripted(ints = listOf(500, 3), floats = listOf(0f), booleans = listOf(true)))
        neva.until { now != null }
        neva.run(1f)
        assertEquals(1f, neva.running, 0f)
        assertTrue(neva.stride > 0f)
        neva.until { now is Neva.Step.Pause }
        assertEquals("mrrp ?", (neva.now as Neva.Step.Pause).say)
        neva.until { now is Neva.Step.Stretch }
        neva.until { now is Neva.Step.Walk }
        neva.until { now == null }
        assertNull(neva.prey)
    }

    @Test
    fun `back on the home screen she comes soon if she is away, and goes on if she is there`() {
        val random = Scripted(ints = listOf(500, 3), floats = listOf(1f, 0f))
        val neva = Neva(random)
        neva.run(1f) { assertFalse(it) }
        neva.comeSoon()
        neva.until { now != null }
        assertTrue(time < 4.2f)
        val drawn = random.floatsDrawn
        neva.comeSoon()
        assertEquals(drawn, random.floatsDrawn)
    }

    @Test
    fun `she blinks every few seconds`() {
        val neva = Neva(Scripted(ints = listOf(500, 0), floats = listOf(0f, 1f), booleans = listOf(true, false)))
        neva.until { now != null }
        var lowest = 3f
        var reopened = false
        neva.run(6f) {
            if (neva.blinkIn > lowest + 1f) reopened = true
            lowest = minOf(lowest, neva.blinkIn)
        }
        assertTrue(lowest < 0f && lowest >= -0.15f - DT)
        assertTrue(reopened)
        assertTrue(abs(neva.t - time) < 1e-6f)
    }

    private companion object {
        const val DT = 1 / 24f
    }
}

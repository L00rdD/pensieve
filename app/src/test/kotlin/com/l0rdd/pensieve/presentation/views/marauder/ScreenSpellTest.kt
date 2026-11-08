package com.l0rdd.pensieve.presentation.views.marauder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.random.Random

class ScreenSpellTest {
    private val coming = SpellTimeline(opening = true, parts = 9)
    private val going = SpellTimeline(opening = false, parts = 9)

    @Test
    fun `the spell comes in about a second and a half, and goes in half a second`() {
        assertEquals(1450L, coming.length)
        assertEquals(SpellTimeline.RETREAT, going.length)
        assertEquals(1050L, SpellTimeline(opening = true, parts = 1).length)
    }

    @Test
    fun `the parts ink in one after the other, from blurred away to sharp`() {
        assertEquals(0f, coming.inked(0, 200), 0f)
        assertEquals(.5f, coming.inked(0, 250 + 190), 1e-3f)
        assertEquals(1f, coming.inked(0, 700), 0f)
        assertEquals(0f, coming.inked(1, 300), 0f)
        assertEquals(1f, coming.inked(8, coming.length), 0f)
        assertTrue(coming.inked(2, 600) > coming.inked(3, 600))
    }

    @Test
    fun `going, every part fades back together as the ink goes back into its drop`() {
        assertEquals(1f, going.inked(0, 0), 0f)
        assertEquals(going.inked(0, 200), going.inked(8, 200), 0f)
        assertEquals(0f, going.inked(5, going.length), 0f)
    }

    @Test
    fun `the ink grows fast then slow, and the tendrils fade once the panel is past half way`() {
        assertEquals(0f, coming.grow(0), 0f)
        assertEquals(1f, coming.grow(SpellTimeline.BLOOM), 0f)
        assertTrue(coming.grow(475) > .5f)
        assertEquals(1f, coming.fade(400), 0f)
        assertEquals(0f, coming.fade(SpellTimeline.BLOOM), 0f)
        assertEquals(1f, going.grow(0), 0f)
        assertEquals(0f, going.grow(going.length), 0f)
        assertEquals(going.grow(100), going.fade(100), 0f)
    }

    @Test
    fun `the drop lands first, then the blobs bloom, the late ones after their delay`() {
        val drop = Bloom(205f, 0f, 1f, 0f)
        val late = Bloom(150f, 90f, .5f, .4f)
        assertEquals(4f, coming.drop(0), 0f)
        assertEquals(14f, coming.drop(400), 0f)
        assertEquals(0f, coming.radius(late, 60), 0f)
        assertTrue(coming.radius(drop, 60) > 0f)
        assertEquals(SpellTimeline.REACH, coming.radius(drop, SpellTimeline.BLOOM), 1e-3f)
        assertEquals(SpellTimeline.REACH * .5f, coming.radius(late, SpellTimeline.BLOOM), 1e-3f)
        assertEquals(0f, going.radius(drop, going.length), 0f)
    }

    @Test
    fun `the blooms start at the drop and spread around and below it`() {
        val all = blooms(205f, 0f)
        assertEquals(8, all.size)
        assertEquals(Bloom(205f, 0f, 1f, 0f), all.first())
        assertTrue(all.drop(1).all { it.y >= 0f && it.delay in .1f..0.45f && it.k in .45f..0.75f })
    }

    @Test
    fun `a step off a side turns back across, off the top or bottom turns up or down`() {
        assertEquals(Step(6f, 50f, PI - 3.0), bounce(Step(2f, 50f, 3.0), 400f, 300f))
        assertEquals(Step(394f, 50f, PI - .2), bounce(Step(399f, 50f, .2), 400f, 300f))
        assertEquals(Step(100f, 294f, -1.0), bounce(Step(100f, 310f, 1.0), 400f, 300f))
        assertEquals(Step(100f, 0f, 1.0), bounce(Step(100f, -3f, -1.0), 400f, 300f))
        assertEquals(Step(100f, 50f, 1.0), bounce(Step(100f, 50f, 1.0), 400f, 300f))
    }

    @Test
    fun `sixteen tendrils walk from the drop and stay inside the panel`() {
        val lines = tendrils(205f, 0f, 410f, 330f, Random(3))
        assertEquals(16, lines.size)
        lines.forEach { line ->
            assertEquals(205f, line.xs[0], 0f); assertEquals(0f, line.ys[0], 0f)
            assertTrue(line.size in 31..80)
            assertTrue(line.xs.all { it in 6f..404f } && line.ys.all { it in 0f..324f })
            assertTrue(line.pace in .7f..1.3f)
        }
    }

    @Test
    fun `a tendril shows more of itself as the ink grows, at its own pace`() {
        val line = Tendril(FloatArray(40), FloatArray(40), pace = 1f, blot = false)
        assertEquals(0, line.shown(0f))
        assertEquals(26, line.shown(.5f))
        assertEquals(40, line.shown(1f))
        assertEquals(20, Tendril(FloatArray(40), FloatArray(40), pace = .5f, blot = true).shown(.77f))
    }
}

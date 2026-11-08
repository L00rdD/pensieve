package com.l0rdd.pensieve.presentation.views.marauder

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/** A blob of ink the panel blooms out of, at ([x], [y]): [k] of the full radius, setting off after [delay] of the bloom. */
data class Bloom(val x: Float, val y: Float, val k: Float, val delay: Float)

/** An ink tendril: the points of its walk, the pace it grows at, and whether it ends on a blot. */
class Tendril(val xs: FloatArray, val ys: FloatArray, val pace: Float, val blot: Boolean) {
    val size: Int get() = xs.size

    /** How many of its points are drawn when the ink has grown [grow] (0..1). */
    fun shown(grow: Float): Int = (size * clamp01(grow * pace * 1.3f)).toInt()
}

/**
 * When each thing of the spell happens, the Marauder's way: the panel blooms out of an ink drop
 * ([BLOOM]), its parts ink in one after the other, the tendrils fading as it settles; or, going,
 * everything goes back into the drop ([RETREAT]). Times are in milliseconds from the cast.
 */
class SpellTimeline(val opening: Boolean, private val parts: Int) {
    /** How long the spell lasts. */
    val length: Long = if (opening) max(BLOOM, LEAD + (parts - 1) * STAGGER + INK_IN) + 100 else RETREAT

    /** How far the bloom (or the retreat, backwards) has gone at [ms]. */
    private fun progress(ms: Long): Float = clamp01(ms.toFloat() / (if (opening) BLOOM else RETREAT))

    /** How far the ink has spread at [ms]: 0 at the drop, 1 all over the panel. */
    fun grow(ms: Long): Float = easeOut(progress(ms)).let { if (opening) it else 1 - it }

    /** How dark the tendrils are at [ms]: they fade out as the panel settles, or as they go back. */
    fun fade(ms: Long): Float = if (opening) 1 - clamp01((progress(ms) - .55f) / .45f) else grow(ms)

    /** How inked part [index] is at [ms]: 0 blurred away, 1 sharp. */
    fun inked(index: Int, ms: Long): Float =
        if (opening) clamp01((ms - LEAD - index * STAGGER).toFloat() / INK_IN) else grow(ms)

    /** The radius of [bloom]'s blob at [ms]. */
    fun radius(bloom: Bloom, ms: Long): Float {
        val reach = if (opening) (grow(ms) - bloom.delay) / (1 - bloom.delay) else grow(ms)
        return max(0f, reach * REACH * bloom.k)
    }

    /** The radius of the drop the tendrils start from at [ms]. */
    fun drop(ms: Long): Float = 4 + 10 * clamp01(grow(ms) * 5)

    companion object {
        const val BLOOM = 950L
        const val INK_IN = 380L
        const val STAGGER = 90L
        const val LEAD = 250L
        const val RETREAT = 520L
        const val REACH = 560f
    }
}

/** The blooming edge: the drop's blob and a few more around it growing late, so the edge is never a clean circle. */
fun blooms(ox: Float, oy: Float, random: Random = Random(7)): List<Bloom> =
    listOf(Bloom(ox, oy, 1f, 0f)) + List(7) {
        val a = PI * random.nextDouble(); val dist = 60 + random.nextFloat() * 140
        Bloom(ox + (cos(a) * dist).toFloat(), oy + (sin(a) * dist).toFloat(), .45f + random.nextFloat() * .3f, .1f + random.nextFloat() * .35f)
    }

/** 16 ink tendrils from the drop at ([ox], [oy]): random walks fanning down, each its pace, kept inside [w] × [h]. */
fun tendrils(ox: Float, oy: Float, w: Float, h: Float, random: Random): List<Tendril> = List(16) { i ->
    val start = PI * (.05 + .9 * i / 15) + (random.nextDouble() - .5) * .3
    walk(Step(ox, oy, start), 30 + random.nextInt(50), w, h, random)
        .let { Tendril(it.map { s -> s.x }.toFloatArray(), it.map { s -> s.y }.toFloatArray(), .7f + random.nextFloat() * .6f, random.nextBoolean()) }
}

/** Where a walk is, and which way it is heading (radians, y down). */
data class Step(val x: Float, val y: Float, val angle: Double)

/** [steps] strides of [STRIDE] from [from], turning a little at random each time, bounced back inside [w] × [h]. */
fun walk(from: Step, steps: Int, w: Float, h: Float, random: Random): List<Step> =
    generateSequence(from) { s ->
        val a = s.angle + (random.nextDouble() - .5) * .5
        bounce(Step(s.x + (cos(a) * STRIDE).toFloat(), s.y + (sin(a) * STRIDE).toFloat(), a), w, h)
    }.take(steps + 1).toList()

/** A step off the panel's edges comes back in, mirrored: off a side it turns back across, off the top or bottom up or down. */
fun bounce(step: Step, w: Float, h: Float): Step {
    var (x, y, a) = step
    if (x < EDGE || x > w - EDGE) { a = PI - a; x = x.coerceIn(EDGE, w - EDGE) }
    if (y < 0 || y > h - EDGE) { a = -a; y = y.coerceIn(0f, h - EDGE) }
    return Step(x, y, a)
}

fun clamp01(x: Float): Float = x.coerceIn(0f, 1f)

fun easeOut(x: Float): Float = 1 - (1 - x) * (1 - x) * (1 - x)

private const val STRIDE = 7f
private const val EDGE = 6f

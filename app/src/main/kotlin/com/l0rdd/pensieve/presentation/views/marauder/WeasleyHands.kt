package com.l0rdd.pensieve.presentation.views.marauder

import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.presentation.views.common.HouseThing
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/** A case of the Weasleys' clock, at [angle] degrees clockwise from the top, with its engraved [label]. */
enum class WeasleyCase(val angle: Float, val label: Int) {
    PERIL(0f, R.string.weasley_peril),
    SHOW(40f, R.string.weasley_case_show),
    LIGHT(75f, R.string.weasley_case_light),
    DIM(110f, R.string.weasley_case_dim),
    ROLLED(145f, R.string.weasley_case_rolled),
    ON_THE_WAY(180f, R.string.weasley_case_on_the_way),
    OPEN(215f, R.string.weasley_case_open),
    AWAY(250f, R.string.weasley_case_away),
    DESK(285f, R.string.weasley_case_desk),
    INTERVAL(320f, R.string.weasley_case_interval);

    /** Halfway to the next case round the dial, where a gold stud goes. */
    val stud: Float get() = (angle + ((entries.getOrNull(ordinal + 1)?.angle ?: 360f) - angle) / 2)
}

/** The case [thing]'s hand points to: where the Weasleys would say it is; all of them in peril when the hub does not answer. */
fun caseOf(thing: HouseThing, house: House): WeasleyCase = if (!house.reachable) WeasleyCase.PERIL else when (thing) {
    HouseThing.Cinema -> if (house.isCinema) WeasleyCase.SHOW else WeasleyCase.INTERVAL
    is HouseThing.Plug -> if (thing.outlet.on) WeasleyCase.LIGHT else WeasleyCase.DIM
    is HouseThing.Projection -> screenCase(thing.screen.position, thing.screen.moving != null)
    is HouseThing.Someone -> if (thing.presence.present) WeasleyCase.DESK else WeasleyCase.AWAY
}

private fun screenCase(position: Int, moving: Boolean): WeasleyCase = when {
    moving || position in 1..99 -> WeasleyCase.ON_THE_WAY
    position >= 100 -> WeasleyCase.OPEN
    else -> WeasleyCase.ROLLED
}

/** Where a hand aims, and whether it is a [short] one (so that its medallion misses its neighbour's). */
data class Aim(val angle: Float, val short: Boolean)

/**
 * Where each thing's hand aims: its case, the screen's anywhere between rolled up and open by its
 * position; hands on one case fanned [fan] degrees apart, one long, one short.
 */
fun aims(things: List<HouseThing>, house: House, fan: Float = 13f): List<Aim> {
    val raw = things.map { rawAngle(it, house) }
    return raw.mapIndexed { i, a ->
        val same = raw.indices.filter { abs(raw[it] - a) < fan * 0.8f }
        val rank = same.indexOf(i)
        Aim(a + (rank - (same.size - 1) / 2f) * fan, rank % 2 == 1)
    }
}

private fun rawAngle(thing: HouseThing, house: House): Float {
    val case = caseOf(thing, house)
    if (thing !is HouseThing.Projection || case == WeasleyCase.PERIL) return case.angle
    return WeasleyCase.ROLLED.angle + (WeasleyCase.OPEN.angle - WeasleyCase.ROLLED.angle) * thing.screen.position / 100f
}

/** A hand trembles while the screen it tells of moves, and all of them when the hub does not answer. */
fun isShaky(thing: HouseThing, house: House): Boolean = !house.reachable || (thing is HouseThing.Projection && thing.screen.moving != null)

/** How far a trembling hand is off its angle at [t] seconds. */
fun tremble(t: Float, angle: Float): Float = sin(t * 23 + angle) * 1.6f + sin(t * 37) * 1.1f

/** A hand on its spring: it goes to its target by the shortest way round, overshoots a little, and settles. */
class Spring(var angle: Float = 0f) {
    private var velocity = 0f

    /** Moves the hand on by [dt] seconds towards [target] (steps of a 60th of a second); returns where it is. */
    fun step(target: Float, dt: Float): Float {
        repeat((dt * 60).roundToInt().coerceIn(0, 12)) {
            val delta = (target - angle + 540f).mod(360f) - 180f
            velocity = (velocity + delta * 0.09f) * 0.74f
            angle += velocity
        }
        return angle
    }
}

/** The engraving on a medallion and in the legend. */
enum class Glyph { CINEMA, TERRARIUM, TORII, HOOP, PLUG, SCREEN, FOOTSTEPS }

/** An engraving, telling its thing's state: [lit] (on, the show on, someone there), [level] how far the screen is down (0..1). */
data class Icon(val glyph: Glyph, val lit: Boolean, val level: Float = 0f)

private val OUTLET_GLYPHS = listOf(
    listOf("terrar", "plant", "vivar") to Glyph.TERRARIUM,
    listOf("torii") to Glyph.TORII,
    listOf("basket", "hoop", "panier") to Glyph.HOOP,
)

/** An outlet's engraving, from its id or its name; a plug for one not known. */
fun glyphOf(outlet: Outlet): Glyph {
    val words = (outlet.id.value + " " + outlet.name).lowercase()
    return OUTLET_GLYPHS.firstOrNull { (keys, _) -> keys.any { it in words } }?.second ?: Glyph.PLUG
}

/** The engraving of [thing] as it is now. */
fun iconOf(thing: HouseThing, house: House): Icon = when (thing) {
    HouseThing.Cinema -> Icon(Glyph.CINEMA, house.isCinema)
    is HouseThing.Plug -> Icon(glyphOf(thing.outlet), thing.outlet.on)
    is HouseThing.Projection -> Icon(Glyph.SCREEN, thing.screen.isDown, thing.screen.position.coerceIn(0, 100) / 100f)
    is HouseThing.Someone -> Icon(Glyph.FOOTSTEPS, thing.presence.present)
}

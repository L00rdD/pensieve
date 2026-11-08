package com.l0rdd.pensieve.presentation.views.common

import android.content.res.Resources
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletSwitch
import com.l0rdd.pensieve.domain.entities.house.Presence
import com.l0rdd.pensieve.domain.entities.house.Screen
import kotlin.math.roundToLong

/** One thing of the house a look shows a row for: the cinema mode, an outlet, the screen, someone at a place. */
sealed interface HouseThing {
    /** Stays the same while the thing is the same, whatever its state. */
    val key: String

    data object Cinema : HouseThing { override val key = "cinema" }
    data class Plug(val outlet: Outlet) : HouseThing { override val key get() = "outlet:" + outlet.id.value }
    data class Projection(val screen: Screen) : HouseThing { override val key get() = "screen:" + screen.id }
    data class Someone(val presence: Presence) : HouseThing { override val key get() = "presence:" + presence.id }
}

/** The things of [house] in its order: the cinema mode, each outlet, the screen, each presence. */
fun houseThings(house: House): List<HouseThing> =
    listOf(HouseThing.Cinema) + house.outlets.map { HouseThing.Plug(it) } + listOfNotNull(house.screen?.let { HouseThing.Projection(it) }) +
        house.presences.map { HouseThing.Someone(it) }

/** Words as a string resource and its arguments, which may be words themselves. */
data class Words(val res: Int, val args: List<Any> = emptyList())

/** [words] in full, its inner words first. */
fun Resources.say(words: Words): String = getString(words.res, *words.args.map { if (it is Words) say(it) else it }.toTypedArray())

/** What a row says of its [thing]: on or off and what next, where the screen is, who is there; the hub out of reach first. */
fun stateWords(thing: HouseThing, house: House): Words = when {
    !house.reachable -> Words(R.string.weasley_unreachable)
    else -> reachableWords(thing, house)
}

private fun reachableWords(thing: HouseThing, house: House): Words = when (thing) {
    HouseThing.Cinema -> Words(if (house.isCinema) R.string.weasley_show else R.string.weasley_interval)
    is HouseThing.Plug -> outletWords(thing.outlet, house)
    is HouseThing.Projection -> screenWords(thing.screen).let { (res, args) -> Words(res, args) }
    is HouseThing.Someone -> presenceWords(thing.presence)
}

private fun outletWords(outlet: Outlet, house: House): Words {
    val state = Words(if (outlet.on) R.string.on else R.string.off)
    val next = outlet.next
    val follows = house.presenceOf(outlet.id)
    return when {
        next != null -> Words(R.string.weasley_scheduled, listOf(state, switchWords(next)))
        follows != null -> Words(R.string.weasley_follows, listOf(state, follows.name.lowercase()))
        else -> state
    }
}

/** "s'éteint à 23:00" */
fun switchWords(next: OutletSwitch): Words =
    Words(if (next.on) R.string.weasley_switch_on_at else R.string.weasley_switch_off_at, listOf("%02d:%02d".format(next.at.hour, next.at.minute)))

private fun presenceWords(presence: Presence): Words {
    val idle = presence.idleSeconds
    return when {
        presence.present -> Words(R.string.weasley_someone)
        idle == null -> Words(R.string.weasley_empty)
        else -> Words(R.string.weasley_empty_since, listOf(maxOf(1L, (idle / 60.0).roundToLong())))
    }
}

/** What OK does on a row, for the hint line; a presence says what its outlet does. */
fun hintOf(thing: HouseThing, house: House): Words = when {
    !house.reachable -> Words(R.string.weasley_hint_peril)
    else -> reachableHint(thing, house)
}

private fun reachableHint(thing: HouseThing, house: House): Words = when (thing) {
    HouseThing.Cinema -> Words(R.string.hint_cinema)
    is HouseThing.Plug -> thing.outlet.next?.let { Words(R.string.weasley_hint_scheduled, listOf(Words(R.string.hint_outlet), switchWords(it))) } ?: Words(R.string.hint_outlet)
    is HouseThing.Projection -> Words(R.string.hint_screen)
    is HouseThing.Someone -> presenceHint(thing.presence, house)
}

private fun presenceHint(presence: Presence, house: House): Words =
    house.outlets.firstOrNull { it.id == presence.outlet }?.let { Words(R.string.hint_presence, listOf(it.name.lowercase())) }
        ?: Words(R.string.hint_presence_alone)

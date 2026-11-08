package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.models.HubStatusModel
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.OutletSwitch
import com.l0rdd.pensieve.domain.entities.house.Presence
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/** The hub's status to the house; [cinemaOutlets] are the outlets the cinema mode switches off. */
class HubStatusMapper(private val cinemaOutlets: Set<String>, private val screenId: String) {
    fun toHouse(model: HubStatusModel) = House(
        outlets = model.outlets.map { Outlet(OutletId(it.id), it.name, it.on, cinema = it.id in cinemaOutlets, next = it.next?.let(::switch)) },
        screen = model.covers.firstOrNull { it.id == screenId }?.let {
            Screen(it.id, it.name, it.position.coerceIn(Screen.UP, Screen.DOWN), when (it.moving) {
                "up" -> ScreenMove.UP
                "down" -> ScreenMove.DOWN
                else -> null
            }, it.target?.coerceIn(Screen.UP, Screen.DOWN))
        },
        reachable = true,
        presences = model.presences.map { Presence(it.id, it.name, it.outlet?.let(::OutletId), it.present, it.idle) },
    )

    /** The schedule's next switch, at the hub's local time; none if the hub wrote a time it cannot be read from. */
    private fun switch(next: HubStatusModel.Next): OutletSwitch? = try {
        OutletSwitch(OffsetDateTime.parse(next.at).toLocalTime(), next.on)
    } catch (_: DateTimeParseException) {
        null
    }
}

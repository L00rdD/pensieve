package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.models.HubStatusModel
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove

/** The hub's status to the house; [cinemaOutlets] are the outlets the cinema mode switches off. */
class HubStatusMapper(private val cinemaOutlets: Set<String>, private val screenId: String) {
    fun toHouse(model: HubStatusModel) = House(
        outlets = model.outlets.map { Outlet(OutletId(it.id), it.name, it.on, cinema = it.id in cinemaOutlets) },
        screen = model.covers.firstOrNull { it.id == screenId }?.let {
            Screen(it.id, it.name, it.position.coerceIn(Screen.UP, Screen.DOWN), when (it.moving) {
                "up" -> ScreenMove.UP
                "down" -> ScreenMove.DOWN
                else -> null
            })
        },
        reachable = true,
    )
}

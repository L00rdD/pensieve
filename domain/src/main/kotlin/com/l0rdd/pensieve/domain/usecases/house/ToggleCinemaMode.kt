package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/**
 * The cinema mode: its outlets off and the screen down. When it is already on, its outlets come
 * back on and the screen stays where it is.
 */
class ToggleCinemaMode(private val house: HouseRepository) : UseCase<Unit, House> {

    override suspend fun invoke(param: House) {
        val cinema = param.outlets.filter { it.cinema }
        if (param.isCinema) {
            cinema.forEach { house.switchOutlet(it.id, on = true) }
            return
        }
        cinema.filter { it.on }.forEach { house.switchOutlet(it.id, on = false) }
        if (param.screen?.isDown == false) house.moveScreen(Screen.DOWN)
    }
}

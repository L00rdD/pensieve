package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Rolls the screen up when it is down, down otherwise. */
class ToggleScreen(private val house: HouseRepository) : UseCase<Unit, Screen> {

    override suspend fun invoke(param: Screen) = house.moveScreen(if (param.isDown) Screen.UP else Screen.DOWN)
}

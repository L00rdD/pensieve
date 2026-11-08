package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Moves the screen, stops it, or declares where it really is. */
class CommandScreen(private val house: HouseRepository) : UseCase<Unit, ScreenCommand> {

    override suspend fun invoke(param: ScreenCommand) = when (param) {
        ScreenCommand.RAISE -> house.moveScreen(Screen.UP)
        ScreenCommand.STOP -> house.stopScreen()
        ScreenCommand.LOWER -> house.moveScreen(Screen.DOWN)
        ScreenCommand.ROLLED_UP -> house.declareScreen(Screen.UP)
        ScreenCommand.OPEN -> house.declareScreen(Screen.DOWN)
    }
}

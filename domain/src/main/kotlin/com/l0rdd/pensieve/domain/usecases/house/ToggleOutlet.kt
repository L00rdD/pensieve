package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Switches an outlet off when it is on, on when it is off. */
class ToggleOutlet(private val house: HouseRepository) : UseCase<Unit, Outlet> {

    override suspend fun invoke(param: Outlet) = house.switchOutlet(param.id, !param.on)
}

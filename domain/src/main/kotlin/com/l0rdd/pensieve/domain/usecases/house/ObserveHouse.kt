package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The house as the hub sees it. */
class ObserveHouse(private val house: HouseRepository) : StreamUseCase<House, NoParam> {

    override fun invoke(param: NoParam): Flow<House> = house.observeHouse().distinctUntilChanged()
}

package com.l0rdd.pensieve.domain.fakes

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Records what was asked of the hub. */
class FakeHouseRepository(private val house: House = House.UNKNOWN) : HouseRepository {
    val switched = mutableListOf<Pair<OutletId, Boolean>>()
    val moved = mutableListOf<Int>()
    val declared = mutableListOf<Int>()
    val pressed = mutableListOf<ScreenMove>()
    var stopped = 0
    override fun observeHouse(): Flow<House> = flowOf(house)
    override suspend fun switchOutlet(id: OutletId, on: Boolean) { switched += id to on }
    override suspend fun moveScreen(position: Int) { moved += position }
    override suspend fun pressScreen(way: ScreenMove) { pressed += way }
    override suspend fun stopScreen() { stopped++ }
    override suspend fun declareScreen(position: Int) { declared += position }
}

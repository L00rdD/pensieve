package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.OutletId
import kotlinx.coroutines.flow.Flow

/** The house, through the hub. */
interface HouseRepository {
    fun observeHouse(): Flow<House>

    suspend fun switchOutlet(id: OutletId, on: Boolean)

    /** Sends the screen to [position], 0 up to 100 down. */
    suspend fun moveScreen(position: Int)

    /** Stops the screen where it is. */
    suspend fun stopScreen()

    /** Tells the hub the screen really is at [position]: nothing moves. */
    suspend fun declareScreen(position: Int)
}

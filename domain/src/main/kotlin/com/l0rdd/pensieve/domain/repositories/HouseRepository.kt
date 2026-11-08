package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import kotlinx.coroutines.flow.Flow

/** The house, through the hub. */
interface HouseRepository {
    fun observeHouse(): Flow<House>

    suspend fun switchOutlet(id: OutletId, on: Boolean)

    /** Sends the screen to [position], 0 up to 100 down. */
    suspend fun moveScreen(position: Int)

    /** Pushes the remote's ▲ or ▼, whatever the hub reckons: the screen goes until it is stopped. */
    suspend fun pressScreen(way: ScreenMove)

    /** Stops the screen where it is. */
    suspend fun stopScreen()

    /** Tells the hub the screen really is at [position]: nothing moves. */
    suspend fun declareScreen(position: Int)
}

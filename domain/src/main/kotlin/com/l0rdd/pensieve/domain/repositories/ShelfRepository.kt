package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.apps.Shelf
import kotlinx.coroutines.flow.Flow

/** How the user arranged the apps, kept on the projector. */
interface ShelfRepository {
    fun observeShelf(): Flow<Shelf>

    suspend fun save(shelf: Shelf)
}

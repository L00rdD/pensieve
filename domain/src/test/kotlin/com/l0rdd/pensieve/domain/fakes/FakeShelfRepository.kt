package com.l0rdd.pensieve.domain.fakes

import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** A shelf kept in memory. */
class FakeShelfRepository(initial: Shelf = Shelf(emptyList(), emptySet(), emptySet())) : ShelfRepository {
    val state = MutableStateFlow(initial)
    override fun observeShelf(): Flow<Shelf> = state
    override suspend fun save(shelf: Shelf) { state.value = shelf }
}

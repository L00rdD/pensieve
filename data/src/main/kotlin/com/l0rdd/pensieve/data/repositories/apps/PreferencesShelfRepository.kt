package com.l0rdd.pensieve.data.repositories.apps

import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.data.datasources.PreferencesDataSource
import com.l0rdd.pensieve.data.models.ShelfModel
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * The shelf, kept in the preferences. On the very first run the [defaultFavorites] that are
 * installed become the favourites, and every app already there counts as seen (none is new).
 */
class PreferencesShelfRepository(
    private val prefs: PreferencesDataSource,
    private val apps: AppsDataSource,
    private val defaultFavorites: List<String>,
) : ShelfRepository {
    private val state = MutableStateFlow<Shelf?>(null)

    override fun observeShelf(): Flow<Shelf> = flow {
        if (state.value == null) state.compareAndSet(null, load())
        emitAll(state.filterNotNull())
    }

    override suspend fun save(shelf: Shelf) {
        state.value = shelf
        withContext(Dispatchers.IO) {
            prefs.write(KEY, ShelfModel(shelf.favorites.map { it.value }, shelf.hidden.map { it.value }.toSet(), shelf.seen.map { it.value }.toSet()).toJson())
        }
    }

    private suspend fun load(): Shelf = withContext(Dispatchers.IO) {
        prefs.read(KEY)?.let { stored ->
            ShelfModel.parse(stored).let { m -> Shelf(m.favorites.map(::AppId), m.hidden.map(::AppId).toSet(), m.seen.map(::AppId).toSet()) }
        } ?: firstRun()
    }

    private suspend fun firstRun(): Shelf {
        val installed = apps.packages()
        return Shelf(defaultFavorites.filter { it in installed }.map(::AppId), emptySet(), installed.map(::AppId).toSet()).also { save(it) }
    }

    private companion object {
        const val KEY = "shelf"
    }
}

package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.usecases.UseCase
import kotlinx.coroutines.flow.first

/** Moves a favourite one place earlier or later. */
class MoveFavorite(private val shelf: ShelfRepository) : UseCase<Unit, MoveFavorite.Param> {

    /** [by] -1 to move [id] earlier, +1 later. */
    data class Param(val id: AppId, val by: Int)

    override suspend fun invoke(param: Param) {
        val current = shelf.observeShelf().first()
        val from = current.favorites.indexOf(param.id)
        if (from < 0) return
        val to = (from + param.by).coerceIn(0, current.favorites.lastIndex)
        if (to == from) return
        val favorites = current.favorites.toMutableList().apply { add(to, removeAt(from)) }
        shelf.save(current.copy(favorites = favorites))
    }
}

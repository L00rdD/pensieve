package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.usecases.UseCase
import kotlinx.coroutines.flow.first

/** Adds an app at the end of the favourites, or takes it out of them. */
class PinFavorite(private val shelf: ShelfRepository) : UseCase<Unit, AppId> {

    override suspend fun invoke(param: AppId) {
        val current = shelf.observeShelf().first()
        val favorites = if (param in current.favorites) current.favorites - param else current.favorites + param
        shelf.save(current.copy(favorites = favorites, seen = current.seen + param))
    }
}

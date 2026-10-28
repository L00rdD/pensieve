package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.usecases.UseCase
import kotlinx.coroutines.flow.first

/** Takes an app off the home screen (it stays installed). */
class HideApp(private val shelf: ShelfRepository) : UseCase<Unit, AppId> {

    override suspend fun invoke(param: AppId) {
        val current = shelf.observeShelf().first()
        shelf.save(current.copy(favorites = current.favorites - param, hidden = current.hidden + param, seen = current.seen + param))
    }
}

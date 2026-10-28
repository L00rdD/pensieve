package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.usecases.UseCase
import kotlinx.coroutines.flow.first

/** Leaves an app where it is, but no longer new. */
class MarkAppSeen(private val shelf: ShelfRepository) : UseCase<Unit, AppId> {

    override suspend fun invoke(param: AppId) {
        val current = shelf.observeShelf().first()
        if (param !in current.seen) shelf.save(current.copy(seen = current.seen + param))
    }
}

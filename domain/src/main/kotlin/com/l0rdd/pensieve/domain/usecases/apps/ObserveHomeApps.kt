package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppKind
import com.l0rdd.pensieve.domain.entities.apps.HomeApps
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.entities.apps.ShelvedApp
import com.l0rdd.pensieve.domain.repositories.AppRepository
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The apps as the home screen arranges them: the favourites in the user's order, then the others
 * by name with the store last. Hidden apps are left out.
 */
class ObserveHomeApps(
    private val apps: AppRepository,
    private val shelf: ShelfRepository,
) : StreamUseCase<HomeApps, NoParam> {

    override fun invoke(param: NoParam): Flow<HomeApps> =
        combine(apps.observeApps(), shelf.observeShelf(), ::arrange)

    private fun arrange(installed: List<App>, shelf: Shelf): HomeApps {
        val byId = installed.associateBy { it.id }
        val shown = { app: App -> ShelvedApp(app, isNew = app.id !in shelf.seen && app.kind == AppKind.APP) }
        val favorites = shelf.favorites.mapNotNull { byId[it] }.filter { it.id !in shelf.hidden }
        val others = installed
            .filter { it.id !in shelf.hidden && it !in favorites }
            .sortedWith(compareBy<App> { it.kind == AppKind.STORE }.thenBy { it.label.lowercase() })
        return HomeApps(favorites.map(shown), others.map(shown))
    }
}

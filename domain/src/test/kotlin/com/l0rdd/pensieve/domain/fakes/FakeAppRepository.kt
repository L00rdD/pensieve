package com.l0rdd.pensieve.domain.fakes

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Installed apps kept in memory; remembers what was launched. */
class FakeAppRepository(initial: List<App> = emptyList()) : AppRepository {
    val state = MutableStateFlow(initial)
    val launched = mutableListOf<AppId>()
    override fun observeApps(): Flow<List<App>> = state
    override suspend fun launch(id: AppId) { launched += id }
}

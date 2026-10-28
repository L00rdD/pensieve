package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import kotlinx.coroutines.flow.Flow

/** The apps installed on the projector. */
interface AppRepository {
    /** Every app that can be opened from the home screen, again whenever one is added or removed. */
    fun observeApps(): Flow<List<App>>

    suspend fun launch(id: AppId)
}

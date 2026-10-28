package com.l0rdd.pensieve.data.repositories.apps

import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.data.mappers.InstalledAppMapper
import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** The installed apps, from the package manager, listed again whenever the packages change. */
class PackageManagerAppRepository(
    private val source: AppsDataSource,
    private val mapper: InstalledAppMapper,
) : AppRepository {

    override fun observeApps(): Flow<List<App>> =
        source.changes().map { source.query().map(mapper::toApp) }.flowOn(Dispatchers.IO)

    override suspend fun launch(id: AppId) = withContext(Dispatchers.Main) { source.launch(id.value) }
}

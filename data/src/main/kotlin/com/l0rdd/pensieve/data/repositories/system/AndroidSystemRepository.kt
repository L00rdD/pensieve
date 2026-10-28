package com.l0rdd.pensieve.data.repositories.system

import android.content.Intent
import android.provider.Settings
import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.domain.repositories.SystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The projector's settings, the JMGO ones among them. */
class AndroidSystemRepository(private val apps: AppsDataSource) : SystemRepository {

    override suspend fun openSettings() = withContext(Dispatchers.Main) { apps.start(Intent(Settings.ACTION_SETTINGS)); Unit }
}

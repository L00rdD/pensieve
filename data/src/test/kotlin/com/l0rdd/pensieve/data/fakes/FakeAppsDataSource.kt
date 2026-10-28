package com.l0rdd.pensieve.data.fakes

import android.content.Intent
import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.data.datasources.InstalledApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** A package manager with a fixed list of packages. */
class FakeAppsDataSource(private val installed: List<String>) : AppsDataSource {
    val launched = mutableListOf<String>()
    override fun query(): List<InstalledApp> = installed.map { InstalledApp(it, it, null) }
    override fun packages(): List<String> = installed
    override fun changes(): Flow<Unit> = flowOf(Unit)
    override fun launch(packageName: String) { launched += packageName }
    override fun start(intent: Intent): Boolean = false
}

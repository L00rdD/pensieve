package com.l0rdd.pensieve.data.repositories.scenes

import com.l0rdd.pensieve.data.datasources.JmgoMiddlewareDataSource
import com.l0rdd.pensieve.data.datasources.SystemSettingsDataSource
import com.l0rdd.pensieve.data.models.DisplayMemoryModel
import com.l0rdd.pensieve.domain.entities.scenes.Scene
import com.l0rdd.pensieve.domain.repositories.SceneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * The scenes JMGO saves in its settings (jmgo_display_memory, the applied one in
 * jmgo_display_memory_current_id), applied through its middleware (key [APPLY_MEMORY]).
 */
class JmgoSceneRepository(
    private val settings: SystemSettingsDataSource,
    private val middleware: JmgoMiddlewareDataSource,
) : SceneRepository {

    override fun observeScenes(): Flow<List<Scene>> = settings.changes(ALL, CURRENT).map {
        val current = settings.read(CURRENT)?.toIntOrNull()
        DisplayMemoryModel.parseList(settings.read(ALL)).sortedBy { it.id }.map { Scene(it.id, it.sceneName.ifEmpty { "Scène ${it.id}" }, it.id == current) }
    }.flowOn(Dispatchers.IO)

    override suspend fun apply(id: Int) { middleware.set(APPLY_MEMORY, id) }

    private companion object {
        const val ALL = "jmgo_display_memory"
        const val CURRENT = "jmgo_display_memory_current_id"
        const val APPLY_MEMORY = 326
    }
}

package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.scenes.Scene
import kotlinx.coroutines.flow.Flow

/** The rooms saved in the projector, and switching from one to another. */
interface SceneRepository {
    /** Every saved scene, again whenever one is saved, renamed, deleted or applied. */
    fun observeScenes(): Flow<List<Scene>>

    suspend fun apply(id: Int)
}

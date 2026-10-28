package com.l0rdd.pensieve.domain.usecases.scenes

import com.l0rdd.pensieve.domain.entities.scenes.Scene
import com.l0rdd.pensieve.domain.repositories.SceneRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The saved scenes, by name. */
class ObserveScenes(private val scenes: SceneRepository) : StreamUseCase<List<Scene>, NoParam> {

    override fun invoke(param: NoParam): Flow<List<Scene>> = scenes.observeScenes().distinctUntilChanged()
}

package com.l0rdd.pensieve.domain.usecases.scenes

import com.l0rdd.pensieve.domain.entities.scenes.Scene
import com.l0rdd.pensieve.domain.repositories.SceneRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Moves the projector to a saved scene; the current one is left as it is. */
class ApplyScene(private val scenes: SceneRepository) : UseCase<Unit, Scene> {

    override suspend fun invoke(param: Scene) {
        if (!param.current) scenes.apply(param.id)
    }
}

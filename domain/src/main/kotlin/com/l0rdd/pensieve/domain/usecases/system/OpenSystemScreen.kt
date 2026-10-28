package com.l0rdd.pensieve.domain.usecases.system

import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.domain.repositories.SystemRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Opens one of the projector's screens, or the quick panel. */
class OpenSystemScreen(private val system: SystemRepository) : UseCase<Unit, SystemScreen> {

    override suspend fun invoke(param: SystemScreen) = system.open(param)
}

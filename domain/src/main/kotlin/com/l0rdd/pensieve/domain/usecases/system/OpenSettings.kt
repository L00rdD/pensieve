package com.l0rdd.pensieve.domain.usecases.system

import com.l0rdd.pensieve.domain.repositories.SystemRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Opens the projector's settings. */
class OpenSettings(private val system: SystemRepository) : UseCase<Unit, NoParam> {

    override suspend fun invoke(param: NoParam) = system.openSettings()
}

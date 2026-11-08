package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.repositories.AppRepository
import com.l0rdd.pensieve.domain.repositories.ProjectorRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Closes every app that can be opened from the home screen: no two players fight over the picture. */
class CloseApps(private val apps: AppRepository, private val projector: ProjectorRepository) : UseCase<Unit, NoParam> {

    override suspend fun invoke(param: NoParam) = projector.close(apps.installed())
}

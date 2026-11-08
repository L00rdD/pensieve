package com.l0rdd.pensieve.domain.fakes

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ProjectorRepository

/** Remembers the apps it was told to close. */
class FakeProjectorRepository : ProjectorRepository {
    val closed = mutableListOf<List<AppId>>()
    override suspend fun close(apps: List<AppId>) { closed += apps }
}

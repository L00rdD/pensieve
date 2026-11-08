package com.l0rdd.pensieve.data.repositories.projector

import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.repositories.ProjectorRepository

/**
 * The projector through the bifrost hub ([hubUrl]), which drives it over adb: it force-stops the
 * apps, and refuses to stop the projector's own ones (the vital process, its maker's services).
 */
class HubProjectorRepository(
    private val http: HttpDataSource,
    private val hubUrl: String,
    private val projectorId: String,
) : ProjectorRepository {

    override suspend fun close(apps: List<AppId>) {
        if (apps.isEmpty()) return
        http.postJson("$hubUrl/api/projectors/$projectorId", """{"close": [${apps.joinToString { "\"${it.value}\"" }}]}""")
    }
}

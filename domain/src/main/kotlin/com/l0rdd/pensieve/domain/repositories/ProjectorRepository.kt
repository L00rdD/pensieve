package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.apps.AppId

/** The projector itself, through the hub. */
interface ProjectorRepository {
    /** Stops [apps], players and services with them; the projector's own services keep running. */
    suspend fun close(apps: List<AppId>)
}

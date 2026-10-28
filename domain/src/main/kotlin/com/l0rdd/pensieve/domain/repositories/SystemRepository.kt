package com.l0rdd.pensieve.domain.repositories

/** The projector's own screens. */
interface SystemRepository {
    suspend fun openSettings()

    suspend fun openStore()
}

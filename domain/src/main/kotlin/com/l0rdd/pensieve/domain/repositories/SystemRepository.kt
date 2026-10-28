package com.l0rdd.pensieve.domain.repositories

/** The projector's own settings screen. */
interface SystemRepository {
    suspend fun openSettings()
}

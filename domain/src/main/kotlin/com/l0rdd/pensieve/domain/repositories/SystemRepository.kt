package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.system.SystemScreen

/** The projector's own screens, and the quick panel. */
interface SystemRepository {
    suspend fun open(screen: SystemScreen)
}

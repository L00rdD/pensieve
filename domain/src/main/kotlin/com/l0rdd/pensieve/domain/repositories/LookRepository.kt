package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.look.Look
import kotlinx.coroutines.flow.Flow

/** The look the user picked for the home screen. */
interface LookRepository {
    fun observeLook(): Flow<Look>

    suspend fun save(look: Look)
}

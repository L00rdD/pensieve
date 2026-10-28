package com.l0rdd.pensieve.domain.usecases.look

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.repositories.LookRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Picks the home screen's look. */
class ChangeLook(private val look: LookRepository) : UseCase<Unit, Look> {

    override suspend fun invoke(param: Look) = look.save(param)
}

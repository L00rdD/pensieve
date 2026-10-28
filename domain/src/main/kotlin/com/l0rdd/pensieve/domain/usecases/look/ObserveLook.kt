package com.l0rdd.pensieve.domain.usecases.look

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.repositories.LookRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The look of the home screen. */
class ObserveLook(private val look: LookRepository) : StreamUseCase<Look, NoParam> {

    override fun invoke(param: NoParam): Flow<Look> = look.observeLook().distinctUntilChanged()
}

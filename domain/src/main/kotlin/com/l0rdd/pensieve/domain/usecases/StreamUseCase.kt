package com.l0rdd.pensieve.domain.usecases

import kotlinx.coroutines.flow.Flow

/** A use case that follows a value as it changes. */
interface StreamUseCase<T, P> {
    operator fun invoke(param: P): Flow<T>
}

package com.l0rdd.pensieve.domain.usecases

/** One action of the domain, with its dependencies given to the constructor. */
interface UseCase<T, P> {
    suspend operator fun invoke(param: P): T
}

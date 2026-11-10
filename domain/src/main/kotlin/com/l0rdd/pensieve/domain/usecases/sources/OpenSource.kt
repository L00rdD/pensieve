package com.l0rdd.pensieve.domain.usecases.sources

import com.l0rdd.pensieve.domain.entities.sources.Source
import com.l0rdd.pensieve.domain.repositories.SourceRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Puts a source's picture on screen, lit or not: a console may still be waking up. */
class OpenSource(private val sources: SourceRepository) : UseCase<Unit, Source> {

    override suspend fun invoke(param: Source) = sources.open(param.id)
}

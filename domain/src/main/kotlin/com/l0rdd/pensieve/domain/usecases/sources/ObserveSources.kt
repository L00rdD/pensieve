package com.l0rdd.pensieve.domain.usecases.sources

import com.l0rdd.pensieve.domain.entities.sources.Source
import com.l0rdd.pensieve.domain.repositories.SourceRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The picture sources, as they are plugged and lit. */
class ObserveSources(private val sources: SourceRepository) : StreamUseCase<List<Source>, NoParam> {

    override fun invoke(param: NoParam): Flow<List<Source>> = sources.observeSources().distinctUntilChanged()
}

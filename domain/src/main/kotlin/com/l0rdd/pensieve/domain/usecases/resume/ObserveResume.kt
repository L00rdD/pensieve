package com.l0rdd.pensieve.domain.usecases.resume

import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** What there is to resume on Jellyfin, or null. */
class ObserveResume(private val resume: ResumeRepository) : StreamUseCase<Resume?, NoParam> {

    override fun invoke(param: NoParam): Flow<Resume?> = resume.observeResume().distinctUntilChanged()
}

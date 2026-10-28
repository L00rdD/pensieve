package com.l0rdd.pensieve.domain.usecases.resume

import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Plays [Resume] again from the start. */
class RestartPlayback(private val resume: ResumeRepository) : UseCase<Unit, Resume> {

    override suspend fun invoke(param: Resume) = resume.play(param, fromStart = true)
}

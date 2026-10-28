package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.resume.Resume
import kotlinx.coroutines.flow.Flow

/** What was left halfway on Jellyfin. */
interface ResumeRepository {
    /** The most recent item to resume, or null when there is none. */
    fun observeResume(): Flow<Resume?>

    /** Opens [resume] in Jellyfin, where it stopped, or from the start. */
    suspend fun play(resume: Resume, fromStart: Boolean)
}

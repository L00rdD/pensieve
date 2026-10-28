package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.models.FeedResumeModel
import com.l0rdd.pensieve.domain.entities.resume.Resume
import kotlin.time.Duration.Companion.seconds

/** Feed entries to something to resume. */
class FeedResumeMapper {
    fun toResume(model: FeedResumeModel) = Resume(
        itemId = model.id,
        title = model.title,
        series = model.series,
        year = model.year,
        runtime = model.runtime.seconds,
        position = model.position.seconds,
        genres = model.genres,
        overview = model.overview,
        posterUrl = model.poster,
        backdropUrl = model.backdrop,
    )
}

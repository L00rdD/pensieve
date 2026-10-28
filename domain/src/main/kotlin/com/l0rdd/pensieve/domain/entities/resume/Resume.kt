package com.l0rdd.pensieve.domain.entities.resume

import kotlin.time.Duration

/** A film or an episode left halfway on Jellyfin. */
data class Resume(
    val itemId: String,
    val title: String,
    val series: String?,
    val year: Int?,
    val runtime: Duration,
    val position: Duration,
    val genres: List<String>,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
) {
    /** How much has been watched, from 0 to 1. */
    val progress: Double
        get() = if (runtime.isPositive()) (position / runtime).coerceIn(0.0, 1.0) else 0.0

    val remaining: Duration
        get() = (runtime - position).coerceAtLeast(Duration.ZERO)
}

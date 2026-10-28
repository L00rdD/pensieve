package com.l0rdd.pensieve.presentation.state.home

import com.l0rdd.pensieve.domain.entities.apps.HomeApps
import com.l0rdd.pensieve.domain.entities.common.Artwork
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.entities.resume.Resume
import java.time.LocalDateTime

/** Everything the home screen shows. [look] is null until the stored one is read. */
data class HomeState(
    val look: Look?,
    val apps: HomeApps,
    val resume: Resume?,
    val poster: Artwork?,
    val house: House,
    val now: LocalDateTime?,
    val menu: HomeMenu?,
) {
    companion object {
        val INITIAL = HomeState(null, HomeApps.EMPTY, null, null, House.UNKNOWN, null, null)
    }
}

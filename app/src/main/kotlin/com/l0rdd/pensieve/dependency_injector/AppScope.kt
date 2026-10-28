package com.l0rdd.pensieve.dependency_injector

import com.l0rdd.pensieve.domain.repositories.AppRepository
import com.l0rdd.pensieve.domain.repositories.ArtworkRepository
import com.l0rdd.pensieve.domain.repositories.ClockRepository
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.repositories.LookRepository
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.repositories.SystemRepository

/** The repositories that live as long as the app. */
class AppScope(
    val apps: AppRepository,
    val shelf: ShelfRepository,
    val resume: ResumeRepository,
    val artwork: ArtworkRepository,
    val house: HouseRepository,
    val look: LookRepository,
    val clock: ClockRepository,
    val system: SystemRepository,
)

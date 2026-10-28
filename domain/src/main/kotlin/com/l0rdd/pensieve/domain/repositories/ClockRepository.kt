package com.l0rdd.pensieve.domain.repositories

import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

/** The local time. */
interface ClockRepository {
    /** The time now, then again every second. */
    fun observeNow(): Flow<LocalDateTime>
}

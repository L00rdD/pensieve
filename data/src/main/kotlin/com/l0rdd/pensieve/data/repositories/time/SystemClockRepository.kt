package com.l0rdd.pensieve.data.repositories.time

import com.l0rdd.pensieve.domain.repositories.ClockRepository
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** The system clock, ticking on the second. */
class SystemClockRepository : ClockRepository {
    override fun observeNow(): Flow<LocalDateTime> = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now)
            delay(1000L - now.nano / 1_000_000)
        }
    }
}

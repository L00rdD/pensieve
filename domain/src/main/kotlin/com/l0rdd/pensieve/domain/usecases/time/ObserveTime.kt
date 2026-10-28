package com.l0rdd.pensieve.domain.usecases.time

import com.l0rdd.pensieve.domain.repositories.ClockRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.StreamUseCase
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

/** The local time, every second. */
class ObserveTime(private val clock: ClockRepository) : StreamUseCase<LocalDateTime, NoParam> {

    override fun invoke(param: NoParam): Flow<LocalDateTime> = clock.observeNow()
}

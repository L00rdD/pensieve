package com.l0rdd.pensieve.domain.entities

import com.l0rdd.pensieve.domain.entities.resume.Resume
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumeTest {
    private fun resume(position: Int, runtime: Int) =
        Resume("id", "Dune", null, 2024, runtime.minutes, position.minutes, emptyList(), "", null, null)

    @Test
    fun `progress and what is left`() {
        val r = resume(position = 72, runtime = 166)
        assertEquals(72.0 / 166, r.progress, 1e-9)
        assertEquals(94.minutes, r.remaining)
    }

    @Test
    fun `a runtime unknown gives no progress`() {
        assertEquals(0.0, resume(position = 10, runtime = 0).progress, 0.0)
    }
}

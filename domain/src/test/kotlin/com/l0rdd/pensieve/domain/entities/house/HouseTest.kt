package com.l0rdd.pensieve.domain.entities.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HouseTest {
    private val desk = Presence("desk", "Bureau", OutletId("basketball-neon"), present = true, idleSeconds = 4)

    @Test
    fun `an outlet follows the presence that drives it`() {
        val house = House(emptyList(), null, reachable = true, presences = listOf(desk))
        assertEquals(desk, house.presenceOf(OutletId("basketball-neon")))
    }

    @Test
    fun `an outlet no presence drives follows none`() {
        assertNull(House(emptyList(), null, reachable = true, presences = listOf(desk)).presenceOf(OutletId("torii")))
    }
}

package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.fakes.FakeHouseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleCinemaModeTest {
    private val neon = Outlet(OutletId("neon"), "Néon", on = true, cinema = true)
    private val grow = Outlet(OutletId("grow"), "Grow", on = true, cinema = false)

    @Test
    fun `switches the cinema outlets off and lowers the screen, the others untouched`() = runTest {
        val hub = FakeHouseRepository()
        ToggleCinemaMode(hub)(House(listOf(neon, grow), Screen("screen", "Écran", 0, null), reachable = true))
        assertEquals(listOf(OutletId("neon") to false), hub.switched)
        assertEquals(listOf(Screen.DOWN), hub.moved)
    }

    @Test
    fun `when already on, its outlets come back and the screen stays down`() = runTest {
        val hub = FakeHouseRepository()
        ToggleCinemaMode(hub)(House(listOf(neon.copy(on = false), grow), Screen("screen", "Écran", 100, null), reachable = true))
        assertEquals(listOf(OutletId("neon") to true), hub.switched)
        assertEquals(emptyList<Int>(), hub.moved)
    }
}

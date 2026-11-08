package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.models.HubStatusModel
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.OutletSwitch
import com.l0rdd.pensieve.domain.entities.house.Presence
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class HubStatusMapperTest {
    @Test
    fun `cinema outlets are flagged, and the screen's move is read`() {
        val house = HubStatusMapper(setOf("basketball-neon"), "screen").toHouse(HubStatusModel(
            listOf(HubStatusModel.Outlet("basketball-neon", "Néon", true), HubStatusModel.Outlet("grow-lamp", "Grow", true)),
            listOf(HubStatusModel.Cover("screen", "Écran", 40, "down", 100)),
        ))
        assertEquals(listOf(true, false), house.outlets.map { it.cinema })
        assertEquals(ScreenMove.DOWN, house.screen?.moving)
        assertEquals(40, house.screen?.position)
        assertEquals(100, house.screen?.target)
    }

    @Test
    fun `the schedule's next switch at the hub's time, and the presences with their outlet`() {
        val house = HubStatusMapper(emptySet(), "screen").toHouse(HubStatusModel(
            listOf(HubStatusModel.Outlet("torii", "Torii", true, HubStatusModel.Next("2026-11-08T23:00+01:00", false))),
            emptyList(),
            listOf(HubStatusModel.Presence("desk", "Bureau", "basketball-neon", false, 2172)),
        ))
        assertEquals(OutletSwitch(LocalTime.of(23, 0), on = false), house.outlets.single().next)
        assertEquals(Presence("desk", "Bureau", OutletId("basketball-neon"), present = false, idleSeconds = 2172), house.presences.single())
    }

    @Test
    fun `a next switch the hub wrote badly is no switch`() {
        val house = HubStatusMapper(emptySet(), "screen").toHouse(HubStatusModel(
            listOf(HubStatusModel.Outlet("torii", "Torii", true, HubStatusModel.Next("ce soir", true))), emptyList(),
        ))
        assertNull(house.outlets.single().next)
    }
}

package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.models.HubStatusModel
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import org.junit.Assert.assertEquals
import org.junit.Test

class HubStatusMapperTest {
    @Test
    fun `cinema outlets are flagged, and the screen's move is read`() {
        val house = HubStatusMapper(setOf("basketball-neon"), "screen").toHouse(HubStatusModel(
            listOf(HubStatusModel.Outlet("basketball-neon", "Néon", true), HubStatusModel.Outlet("grow-lamp", "Grow", true)),
            listOf(HubStatusModel.Cover("screen", "Écran", 40, "down")),
        ))
        assertEquals(listOf(true, false), house.outlets.map { it.cinema })
        assertEquals(ScreenMove.DOWN, house.screen?.moving)
        assertEquals(40, house.screen?.position)
    }
}

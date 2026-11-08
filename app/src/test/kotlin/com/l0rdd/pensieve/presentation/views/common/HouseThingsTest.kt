package com.l0rdd.pensieve.presentation.views.common

import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.OutletSwitch
import com.l0rdd.pensieve.domain.entities.house.Presence
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class HouseThingsTest {
    private val terrarium = Outlet(OutletId("terrarium"), "Terrarium", on = false, cinema = false)
    private val torii = Outlet(OutletId("torii"), "Torii", on = true, cinema = false, next = OutletSwitch(LocalTime.of(23, 0), on = false))
    private val neon = Outlet(OutletId("basketball-neon"), "Néon basket", on = false, cinema = true)
    private val screen = Screen("screen", "Écran", 0, null)
    private val desk = Presence("desk", "Bureau", OutletId("basketball-neon"), present = false, idleSeconds = 36 * 60L)
    private val house = House(listOf(terrarium, torii, neon), screen, reachable = true, presences = listOf(desk))

    @Test
    fun `the things come in the house's order, each with a key of its own`() {
        val things = houseThings(house)
        assertEquals(listOf(HouseThing.Cinema, HouseThing.Plug(terrarium), HouseThing.Plug(torii), HouseThing.Plug(neon), HouseThing.Projection(screen), HouseThing.Someone(desk)), things)
        assertEquals(listOf("cinema", "outlet:terrarium", "outlet:torii", "outlet:basketball-neon", "screen:screen", "presence:desk"), things.map { it.key })
        assertEquals(listOf(HouseThing.Cinema), houseThings(House.UNKNOWN))
    }

    @Test
    fun `the cinema is in its interval or its show`() {
        assertEquals(Words(R.string.weasley_interval), stateWords(HouseThing.Cinema, house))
        val show = house.copy(screen = screen.copy(position = 100))
        assertEquals(Words(R.string.weasley_show), stateWords(HouseThing.Cinema, show))
    }

    @Test
    fun `an outlet is on or off, then says its next switch, or that it follows a presence`() {
        assertEquals(Words(R.string.off), stateWords(HouseThing.Plug(terrarium), house))
        assertEquals(Words(R.string.weasley_scheduled, listOf(Words(R.string.on), Words(R.string.weasley_switch_off_at, listOf("23:00")))), stateWords(HouseThing.Plug(torii), house))
        assertEquals(Words(R.string.weasley_follows, listOf(Words(R.string.off), "bureau")), stateWords(HouseThing.Plug(neon), house))
        assertEquals(Words(R.string.weasley_switch_on_at, listOf("07:05")), switchWords(OutletSwitch(LocalTime.of(7, 5), on = true)))
    }

    @Test
    fun `the screen says where it is`() {
        assertEquals(Words(R.string.screen_rolled), stateWords(HouseThing.Projection(screen), house))
        assertEquals(Words(R.string.screen_lowering, listOf(40)), stateWords(HouseThing.Projection(screen.copy(position = 40, moving = ScreenMove.DOWN)), house))
    }

    @Test
    fun `someone is there, or the place has been empty for some minutes, at least one`() {
        assertEquals(Words(R.string.weasley_someone), stateWords(HouseThing.Someone(desk.copy(present = true)), house))
        assertEquals(Words(R.string.weasley_empty_since, listOf(36L)), stateWords(HouseThing.Someone(desk), house))
        assertEquals(Words(R.string.weasley_empty_since, listOf(1L)), stateWords(HouseThing.Someone(desk.copy(idleSeconds = 5)), house))
        assertEquals(Words(R.string.weasley_empty), stateWords(HouseThing.Someone(desk.copy(idleSeconds = null)), house))
    }

    @Test
    fun `out of reach, every row says the hub does not answer, and so does the hint`() {
        val lost = house.copy(reachable = false)
        houseThings(lost).forEach {
            assertEquals(Words(R.string.weasley_unreachable), stateWords(it, lost))
            assertEquals(Words(R.string.weasley_hint_peril), hintOf(it, lost))
        }
    }

    @Test
    fun `each row's hint says what OK does, a scheduled outlet adds its schedule, a presence what its outlet does`() {
        assertEquals(Words(R.string.hint_cinema), hintOf(HouseThing.Cinema, house))
        assertEquals(Words(R.string.hint_outlet), hintOf(HouseThing.Plug(terrarium), house))
        assertEquals(Words(R.string.weasley_hint_scheduled, listOf(Words(R.string.hint_outlet), Words(R.string.weasley_switch_off_at, listOf("23:00")))), hintOf(HouseThing.Plug(torii), house))
        assertEquals(Words(R.string.hint_screen), hintOf(HouseThing.Projection(screen), house))
        assertEquals(Words(R.string.hint_presence, listOf("néon basket")), hintOf(HouseThing.Someone(desk), house))
        assertEquals(Words(R.string.hint_presence_alone), hintOf(HouseThing.Someone(desk.copy(outlet = null)), house))
    }
}

package com.l0rdd.pensieve.presentation.views.marauder

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.Presence
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import com.l0rdd.pensieve.presentation.views.common.HouseThing
import com.l0rdd.pensieve.presentation.views.common.houseThings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeasleyHandsTest {
    private fun outlet(id: String, name: String = id, on: Boolean = false, cinema: Boolean = false) = Outlet(OutletId(id), name, on, cinema)
    private val screen = Screen("screen", "Écran", 0, null)
    private val desk = Presence("desk", "Bureau", OutletId("basketball-neon"), present = false, idleSeconds = 60)
    private val house = House(listOf(outlet("terrarium"), outlet("torii", on = true), outlet("basketball-neon", "Néon basket", cinema = true)), screen, reachable = true, presences = listOf(desk))

    @Test
    fun `each thing's hand points to where the Weasleys would say it is`() {
        val cases = houseThings(house).map { caseOf(it, house) }
        assertEquals(listOf(WeasleyCase.INTERVAL, WeasleyCase.DIM, WeasleyCase.LIGHT, WeasleyCase.DIM, WeasleyCase.ROLLED, WeasleyCase.AWAY), cases)
        val show = house.copy(screen = screen.copy(position = 100), presences = listOf(desk.copy(present = true)))
        assertEquals(listOf(WeasleyCase.SHOW, WeasleyCase.OPEN, WeasleyCase.DESK), listOf(HouseThing.Cinema, HouseThing.Projection(show.screen!!), HouseThing.Someone(show.presences[0])).map { caseOf(it, show) })
    }

    @Test
    fun `a screen half down or moving is on its way, and out of reach everything is in peril`() {
        assertEquals(WeasleyCase.ON_THE_WAY, caseOf(HouseThing.Projection(screen.copy(position = 50)), house))
        assertEquals(WeasleyCase.ON_THE_WAY, caseOf(HouseThing.Projection(screen.copy(moving = ScreenMove.DOWN)), house))
        val lost = house.copy(reachable = false)
        assertTrue(houseThings(lost).all { caseOf(it, lost) == WeasleyCase.PERIL })
    }

    @Test
    fun `the cases go clockwise from peril, with a stud halfway to the next`() {
        assertEquals(listOf(0f, 40f, 75f, 110f, 145f, 180f, 215f, 250f, 285f, 320f), WeasleyCase.entries.map { it.angle })
        assertEquals(20f, WeasleyCase.PERIL.stud)
        assertEquals(340f, WeasleyCase.INTERVAL.stud)
    }

    @Test
    fun `hands on one case fan out, one long one short, and a lone hand stays on its case`() {
        val aims = aims(houseThings(house), house)
        assertEquals(Aim(320f, false), aims[0])
        // terrarium and the neon both dim
        assertEquals(Aim(110f - 6.5f, false), aims[1])
        assertEquals(Aim(110f + 6.5f, true), aims[3])
        assertEquals(Aim(75f, false), aims[2])
        assertEquals(Aim(250f, false), aims[5])
    }

    @Test
    fun `the screen's hand goes between rolled up and open by its position, and to peril with the rest`() {
        val half = house.copy(screen = screen.copy(position = 50, moving = ScreenMove.DOWN))
        assertEquals(180f, aims(listOf(HouseThing.Projection(half.screen!!)), half)[0].angle)
        val lost = house.copy(reachable = false)
        val all = aims(houseThings(lost), lost)
        assertEquals(listOf(-32.5f, -19.5f, -6.5f, 6.5f, 19.5f, 32.5f), all.map { it.angle })
        assertEquals(listOf(false, true, false, true, false, true), all.map { it.short })
    }

    @Test
    fun `hands tremble while the screen moves or the hub does not answer`() {
        assertTrue(isShaky(HouseThing.Projection(screen.copy(moving = ScreenMove.UP)), house))
        assertFalse(isShaky(HouseThing.Projection(screen), house))
        assertFalse(isShaky(HouseThing.Cinema, house))
        assertTrue(isShaky(HouseThing.Cinema, house.copy(reachable = false)))
        assertEquals(0f, tremble(0f, 0f), 0f)
        assertTrue(kotlin.math.abs(tremble(1.3f, 40f)) <= 2.7f)
    }

    @Test
    fun `a hand springs to its target the short way round, overshoots, and settles`() {
        val spring = Spring()
        val first = spring.step(90f, 1 / 60f)
        assertEquals(90f * 0.09f * 0.74f, first, 0.001f)
        var max = 0f
        repeat(120) { max = maxOf(max, spring.step(90f, 1 / 24f)) }
        assertTrue(max > 90f)
        assertEquals(90f, spring.angle, 0.01f)
        val back = Spring(10f)
        repeat(200) { back.step(350f, 1 / 24f) }
        assertEquals(-10f, back.angle, 0.01f)
        assertEquals(-10f, back.step(350f, 0f), 0.01f)
    }

    @Test
    fun `an outlet's engraving comes from its id or name, a plug when unknown`() {
        assertEquals(Glyph.TERRARIUM, glyphOf(outlet("terrarium")))
        assertEquals(Glyph.TORII, glyphOf(outlet("lamp-2", "Torii")))
        assertEquals(Glyph.HOOP, glyphOf(outlet("basketball-neon")))
        assertEquals(Glyph.PLUG, glyphOf(outlet("lamp", "Lampe")))
    }

    @Test
    fun `each engraving tells its thing's state`() {
        val icons = houseThings(house).map { iconOf(it, house) }
        assertEquals(listOf(Icon(Glyph.CINEMA, false), Icon(Glyph.TERRARIUM, false), Icon(Glyph.TORII, true), Icon(Glyph.HOOP, false), Icon(Glyph.SCREEN, false, 0f), Icon(Glyph.FOOTSTEPS, false)), icons)
        assertEquals(Icon(Glyph.SCREEN, false, 0.4f), iconOf(HouseThing.Projection(screen.copy(position = 40)), house))
        val show = house.copy(screen = screen.copy(position = 100))
        assertEquals(Icon(Glyph.CINEMA, true), iconOf(HouseThing.Cinema, show))
    }
}

package com.l0rdd.pensieve.presentation.state.home

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.usecases.apps.HideApp
import com.l0rdd.pensieve.domain.usecases.apps.LaunchApp
import com.l0rdd.pensieve.domain.usecases.apps.MarkAppSeen
import com.l0rdd.pensieve.domain.usecases.apps.MoveFavorite
import com.l0rdd.pensieve.domain.usecases.apps.ObserveHomeApps
import com.l0rdd.pensieve.domain.usecases.apps.PinFavorite
import com.l0rdd.pensieve.domain.usecases.house.ObserveHouse
import com.l0rdd.pensieve.domain.usecases.house.ToggleCinemaMode
import com.l0rdd.pensieve.domain.usecases.house.ToggleOutlet
import com.l0rdd.pensieve.domain.usecases.house.ToggleScreen
import com.l0rdd.pensieve.domain.usecases.look.ChangeLook
import com.l0rdd.pensieve.domain.usecases.look.ObserveLook
import com.l0rdd.pensieve.domain.usecases.resume.LoadArtwork
import com.l0rdd.pensieve.domain.usecases.resume.ObserveResume
import com.l0rdd.pensieve.domain.usecases.resume.RestartPlayback
import com.l0rdd.pensieve.domain.usecases.resume.ResumePlayback
import com.l0rdd.pensieve.domain.usecases.system.OpenSystemScreen
import com.l0rdd.pensieve.domain.usecases.time.ObserveTime
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val netflix = App(AppId("netflix"), "Netflix", null)
    private val vlc = App(AppId("vlc"), "VLC", null)
    private val dune = Resume("abc", "Dune", null, 2024, 166.minutes, 72.minutes, emptyList(), "", "/Items/abc/Images/Primary", null)
    private val neon = Outlet(OutletId("neon"), "Néon", on = true, cinema = true)
    private val house = House(listOf(neon), Screen("screen", "Écran", 0, null), reachable = true)
    private val now = LocalDateTime.of(2026, 10, 28, 21, 34)

    private val apps = FakeApps(listOf(netflix, vlc))
    private val shelf = FakeShelf(Shelf(listOf(netflix.id), emptySet(), setOf(netflix.id, vlc.id)))
    private val resume = FakeResume(dune)
    private val hub = FakeHouse(house)
    private val look = FakeLook()
    private val system = FakeSystem()

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = HomeViewModel(HomeUseCases(
        ObserveLook(look), ChangeLook(look), ObserveHomeApps(apps, shelf), LaunchApp(apps, shelf), PinFavorite(shelf), MoveFavorite(shelf),
        HideApp(shelf), MarkAppSeen(shelf), ObserveResume(resume), ResumePlayback(resume), RestartPlayback(resume), LoadArtwork(FakeArtwork()),
        ObserveHouse(hub), ToggleOutlet(hub), ToggleScreen(hub), ToggleCinemaMode(hub), ObserveTime(FakeClock(now)), OpenSystemScreen(system),
    ))

    @Test
    fun `the state follows the look, the apps, what to resume and its poster, the house and the time`() {
        val state = viewModel().state.value
        assertEquals(Look.JARVIS, state.look)
        assertEquals(listOf("netflix"), state.apps.favorites.map { it.app.id.value })
        assertEquals("Dune", state.resume?.title)
        assertNotNull(state.poster)
        assertEquals(house, state.house)
        assertEquals(now, state.now)
    }

    @Test
    fun `OK opens the app, resume plays where it stopped`() {
        val vm = viewModel()
        vm.dispatch(HomeEvent.AppPicked(vlc.id))
        vm.dispatch(HomeEvent.ResumePicked)
        vm.dispatch(HomeEvent.RestartPicked)
        assertEquals(listOf(vlc.id), apps.launched)
        assertEquals(listOf(dune to false, dune to true), resume.played)
    }

    @Test
    fun `OK held opens the arranging menu, pinning closes it and pins`() {
        val vm = viewModel()
        vm.dispatch(HomeEvent.AppHeld(vlc.id))
        assertEquals(HomeMenu.ArrangeApp(vlc.id, "VLC", favorite = false), vm.state.value.menu)
        vm.dispatch(HomeEvent.Arranged(vlc.id, Arrange.PIN))
        assertEquals(null, vm.state.value.menu)
        assertEquals(listOf(netflix.id, vlc.id), shelf.state.value.favorites)
    }

    @Test
    fun `the house, cinema mode then the screen`() {
        val vm = viewModel()
        vm.dispatch(HomeEvent.CinemaPicked)
        vm.dispatch(HomeEvent.ScreenPicked)
        assertEquals(listOf(neon.id to false), hub.switched)
        assertEquals(listOf(Screen.DOWN, Screen.DOWN), hub.moved)
    }

    @Test
    fun `settings open on OK, the look menu on OK held, and a look picked is kept`() {
        val vm = viewModel()
        vm.dispatch(HomeEvent.SettingsPicked)
        vm.dispatch(HomeEvent.SettingsHeld)
        assertEquals(listOf(com.l0rdd.pensieve.domain.entities.system.SystemScreen.SETTINGS), system.opened)
        assertEquals(HomeMenu.PickLook, vm.state.value.menu)
        vm.dispatch(HomeEvent.LookPicked(Look.MARAUDER))
        assertEquals(Look.MARAUDER, vm.state.value.look)
        assertEquals(null, vm.state.value.menu)
    }
}

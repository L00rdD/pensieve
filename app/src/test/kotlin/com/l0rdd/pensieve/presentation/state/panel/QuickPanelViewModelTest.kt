package com.l0rdd.pensieve.presentation.state.panel

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.entities.scenes.Scene
import com.l0rdd.pensieve.domain.entities.sources.Source
import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.domain.repositories.ProjectorRepository
import com.l0rdd.pensieve.domain.repositories.SceneRepository
import com.l0rdd.pensieve.domain.repositories.SourceRepository
import com.l0rdd.pensieve.domain.usecases.apps.CloseApps
import com.l0rdd.pensieve.domain.usecases.house.ObserveHouse
import com.l0rdd.pensieve.domain.usecases.house.ToggleCinemaMode
import com.l0rdd.pensieve.domain.usecases.house.ToggleScreen
import com.l0rdd.pensieve.domain.usecases.look.ChangeLook
import com.l0rdd.pensieve.domain.usecases.look.ObserveLook
import com.l0rdd.pensieve.domain.usecases.scenes.ApplyScene
import com.l0rdd.pensieve.domain.usecases.scenes.ObserveScenes
import com.l0rdd.pensieve.domain.usecases.sources.ObserveSources
import com.l0rdd.pensieve.domain.usecases.sources.OpenSource
import com.l0rdd.pensieve.domain.usecases.system.OpenSystemScreen
import com.l0rdd.pensieve.presentation.state.home.FakeApps
import com.l0rdd.pensieve.presentation.state.home.FakeHouse
import com.l0rdd.pensieve.presentation.state.home.FakeLook
import com.l0rdd.pensieve.presentation.state.home.FakeSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickPanelViewModelTest {
    private val netflix = App(AppId("com.netflix.ninja"), "Netflix", null)
    private val vlc = App(AppId("org.videolan.vlc"), "VLC", null)
    private val neon = Outlet(OutletId("neon"), "Néon", on = true, cinema = true)
    private val house = House(listOf(neon), Screen("screen", "Écran", 0, null), reachable = true)
    private val salon = Scene(1, "Salon", current = true)
    private val chambre = Scene(2, "Chambre", current = false)

    private val look = FakeLook()
    private val hub = FakeHouse(house)
    private val system = FakeSystem()
    private val scenes = FakeScenes(listOf(salon, chambre))
    private val projector = FakeProjector()
    private val ps5 = Source("hdmi/HW4", "PS5", lit = true)
    private val sources = FakeSources(listOf(ps5))

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = QuickPanelViewModel(
        ObserveLook(look), ChangeLook(look), ObserveHouse(hub), ToggleCinemaMode(hub), ToggleScreen(hub),
        OpenSystemScreen(system), ObserveScenes(scenes), ApplyScene(scenes), CloseApps(FakeApps(listOf(netflix, vlc)), projector),
        ObserveSources(sources), OpenSource(sources),
    )

    @Test
    fun `the state follows the look, the house, the rooms and the sources`() {
        val state = viewModel().state.value
        assertEquals(Look.JARVIS, state.look)
        assertEquals(house, state.house)
        assertEquals(listOf(salon, chambre), state.scenes)
        assertEquals(listOf(ps5), state.sources)
        assertFalse(state.done)
    }

    @Test
    fun `closing the apps closes them all and leaves the panel`() {
        val vm = viewModel()
        vm.dispatch(QuickPanelEvent.CloseAppsPicked)
        assertEquals(listOf(listOf(netflix.id, vlc.id)), projector.closed)
        assertTrue(vm.state.value.done)
    }

    @Test
    fun `a source opens and leaves the panel`() {
        val vm = viewModel()
        vm.dispatch(QuickPanelEvent.SourcePicked(ps5))
        assertEquals(listOf("hdmi/HW4"), sources.opened)
        assertTrue(vm.state.value.done)
    }

    @Test
    fun `a system screen opens and leaves the panel`() {
        val vm = viewModel()
        vm.dispatch(QuickPanelEvent.ScreenPicked(SystemScreen.KEYSTONE))
        assertEquals(listOf(SystemScreen.KEYSTONE), system.opened)
        assertTrue(vm.state.value.done)
    }

    @Test
    fun `the house's toggles, the rooms and the look keep the panel open`() {
        val vm = viewModel()
        vm.dispatch(QuickPanelEvent.CinemaPicked)
        vm.dispatch(QuickPanelEvent.ProjectionScreenPicked)
        vm.dispatch(QuickPanelEvent.ScenePicked(chambre))
        vm.dispatch(QuickPanelEvent.LookCycled)
        assertEquals(listOf(neon.id to false), hub.switched)
        assertEquals(listOf(Screen.DOWN, Screen.DOWN), hub.moved)
        assertEquals(listOf(2), scenes.applied)
        assertEquals(Look.entries[(Look.JARVIS.ordinal + 1) % Look.entries.size], look.state.value)
        assertFalse(vm.state.value.done)
    }

    private class FakeScenes(private val scenes: List<Scene>) : SceneRepository {
        val applied = mutableListOf<Int>()
        override fun observeScenes(): Flow<List<Scene>> = flowOf(scenes)
        override suspend fun apply(id: Int) { applied += id }
    }

    private class FakeSources(private val sources: List<Source>) : SourceRepository {
        val opened = mutableListOf<String>()
        override fun observeSources(): Flow<List<Source>> = flowOf(sources)
        override suspend fun open(id: String) { opened += id }
    }

    private class FakeProjector : ProjectorRepository {
        val closed = mutableListOf<List<AppId>>()
        override suspend fun close(apps: List<AppId>) { closed += apps }
    }
}

package com.l0rdd.pensieve.presentation.state.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.apps.MoveFavorite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The home screen, as events in and one state out. It follows the look, the apps, what to resume,
 * the house and the time; the user's events call the use cases. A failing command (a hub that does
 * not answer) changes nothing: the next reading of the house shows what really happened.
 */
class HomeViewModel(private val useCases: HomeUseCases) : ViewModel() {
    private val _state = MutableStateFlow(HomeState.INITIAL)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        follow { useCases.observeLook(NoParam).collect { look -> _state.update { it.copy(look = look) } } }
        follow { useCases.observeHomeApps(NoParam).collect { apps -> _state.update { it.copy(apps = apps) } } }
        follow { useCases.observeHouse(NoParam).collect { house -> _state.update { it.copy(house = house) } } }
        follow { useCases.observeTime(NoParam).collect { now -> _state.update { it.copy(now = now) } } }
        follow {
            useCases.observeResume(NoParam).collectLatest { resume ->
                _state.update { it.copy(resume = resume, poster = if (resume?.posterUrl == it.resume?.posterUrl) it.poster else null) }
                val poster = resume?.posterUrl?.let { useCases.loadArtwork(it) }
                _state.update { it.copy(poster = poster) }
            }
        }
    }

    fun dispatch(event: HomeEvent) {
        viewModelScope.launch { runCatching { handle(event) } }
    }

    private suspend fun handle(event: HomeEvent) {
        val state = _state.value
        when (event) {
            is HomeEvent.AppPicked -> useCases.launchApp(event.id)
            is HomeEvent.AppHeld -> openArranging(state, event.id)
            is HomeEvent.Arranged -> {
                _state.update { it.copy(menu = null) }
                arrange(event.id, event.arrange)
            }
            HomeEvent.MenuClosed -> _state.update { it.copy(menu = null) }
            HomeEvent.ResumePicked -> state.resume?.let { useCases.resumePlayback(it) }
            HomeEvent.RestartPicked -> state.resume?.let { useCases.restartPlayback(it) }
            HomeEvent.CinemaPicked -> useCases.toggleCinemaMode(state.house)
            is HomeEvent.OutletPicked -> useCases.toggleOutlet(event.outlet)
            HomeEvent.ScreenPicked -> _state.update { it.copy(menu = HomeMenu.CommandScreen) }
            is HomeEvent.ScreenCommanded -> useCases.commandScreen(event.command)
            HomeEvent.SettingsPicked -> useCases.openSystemScreen(SystemScreen.SETTINGS)
            HomeEvent.SettingsHeld -> _state.update { it.copy(menu = HomeMenu.PickLook) }
            is HomeEvent.LookPicked -> {
                _state.update { it.copy(menu = null) }
                useCases.changeLook(event.look)
            }
        }
    }

    /** OK held on an app: it is seen, and its arranging menu opens. */
    private suspend fun openArranging(state: HomeState, id: AppId) {
        val app = (state.apps.favorites + state.apps.others).firstOrNull { it.app.id == id } ?: return
        useCases.markAppSeen(id)
        _state.update { it.copy(menu = HomeMenu.ArrangeApp(id, app.app.label, state.apps.favorites.contains(app))) }
    }

    private suspend fun arrange(id: AppId, arrange: Arrange) {
        when (arrange) {
            Arrange.PIN -> useCases.pinFavorite(id)
            Arrange.EARLIER -> useCases.moveFavorite(MoveFavorite.Param(id, -1))
            Arrange.LATER -> useCases.moveFavorite(MoveFavorite.Param(id, +1))
            Arrange.HIDE -> useCases.hideApp(id)
        }
    }

    private fun follow(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}

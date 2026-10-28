package com.l0rdd.pensieve.presentation.state.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            is HomeEvent.AppHeld -> {
                val app = (state.apps.favorites + state.apps.others).firstOrNull { it.app.id == event.id } ?: return
                useCases.markAppSeen(event.id)
                _state.update { it.copy(menu = HomeMenu.ArrangeApp(event.id, app.app.label, state.apps.favorites.contains(app))) }
            }
            is HomeEvent.Arranged -> {
                _state.update { it.copy(menu = null) }
                when (event.arrange) {
                    Arrange.PIN -> useCases.pinFavorite(event.id)
                    Arrange.EARLIER -> useCases.moveFavorite(MoveFavorite.Param(event.id, -1))
                    Arrange.LATER -> useCases.moveFavorite(MoveFavorite.Param(event.id, +1))
                    Arrange.HIDE -> useCases.hideApp(event.id)
                }
            }
            HomeEvent.MenuClosed -> _state.update { it.copy(menu = null) }
            HomeEvent.ResumePicked -> state.resume?.let { useCases.resumePlayback(it) }
            HomeEvent.RestartPicked -> state.resume?.let { useCases.restartPlayback(it) }
            HomeEvent.CinemaPicked -> useCases.toggleCinemaMode(state.house)
            is HomeEvent.OutletPicked -> useCases.toggleOutlet(event.outlet)
            HomeEvent.ScreenPicked -> state.house.screen?.let { useCases.toggleScreen(it) }
            HomeEvent.SettingsPicked -> useCases.openSystemScreen(SystemScreen.SETTINGS)
            HomeEvent.SettingsHeld -> _state.update { it.copy(menu = HomeMenu.PickLook) }
            is HomeEvent.LookPicked -> {
                _state.update { it.copy(menu = null) }
                useCases.changeLook(event.look)
            }
        }
    }

    private fun follow(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}

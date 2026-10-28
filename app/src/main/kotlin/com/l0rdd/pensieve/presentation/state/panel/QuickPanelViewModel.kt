package com.l0rdd.pensieve.presentation.state.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.usecases.NoParam
import com.l0rdd.pensieve.domain.usecases.house.ObserveHouse
import com.l0rdd.pensieve.domain.usecases.house.ToggleCinemaMode
import com.l0rdd.pensieve.domain.usecases.house.ToggleScreen
import com.l0rdd.pensieve.domain.usecases.look.ChangeLook
import com.l0rdd.pensieve.domain.usecases.look.ObserveLook
import com.l0rdd.pensieve.domain.usecases.scenes.ApplyScene
import com.l0rdd.pensieve.domain.usecases.scenes.ObserveScenes
import com.l0rdd.pensieve.domain.usecases.system.OpenSystemScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The quick panel the ⚙ key opens, in any app: the rooms saved in the projector, its screens, the
 * cinema mode, the projection screen and the look. Opening a screen closes the panel; the house's toggles keep it open.
 */
class QuickPanelViewModel(
    private val observeLook: ObserveLook,
    private val changeLook: ChangeLook,
    private val observeHouse: ObserveHouse,
    private val toggleCinemaMode: ToggleCinemaMode,
    private val toggleScreen: ToggleScreen,
    private val openSystemScreen: OpenSystemScreen,
    private val observeScenes: ObserveScenes,
    private val applyScene: ApplyScene,
) : ViewModel() {
    private val _state = MutableStateFlow(QuickPanelState.INITIAL)
    val state: StateFlow<QuickPanelState> = _state.asStateFlow()

    init {
        viewModelScope.launch { runCatching { observeLook(NoParam).collect { look -> _state.update { it.copy(look = look) } } } }
        viewModelScope.launch { runCatching { observeHouse(NoParam).collect { house -> _state.update { it.copy(house = house) } } } }
        viewModelScope.launch { runCatching { observeScenes(NoParam).collect { scenes -> _state.update { it.copy(scenes = scenes) } } } }
    }

    fun dispatch(event: QuickPanelEvent) {
        viewModelScope.launch {
            runCatching {
                val state = _state.value
                when (event) {
                    is QuickPanelEvent.ScreenPicked -> { openSystemScreen(event.screen); _state.update { it.copy(done = true) } }
                    QuickPanelEvent.CinemaPicked -> toggleCinemaMode(state.house)
                    QuickPanelEvent.ProjectionScreenPicked -> state.house.screen?.let { toggleScreen(it) }
                    is QuickPanelEvent.ScenePicked -> applyScene(event.scene)
                    QuickPanelEvent.LookCycled -> state.look?.let { changeLook(Look.entries[(it.ordinal + 1) % Look.entries.size]) }
                }
            }
        }
    }
}

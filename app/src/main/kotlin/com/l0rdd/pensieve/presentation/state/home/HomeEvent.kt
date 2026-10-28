package com.l0rdd.pensieve.presentation.state.home

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.look.Look

/** What the user does on the home screen. */
sealed interface HomeEvent {
    data class AppPicked(val id: AppId) : HomeEvent
    data class AppHeld(val id: AppId) : HomeEvent
    data class Arranged(val id: AppId, val arrange: Arrange) : HomeEvent
    data object MenuClosed : HomeEvent
    data object ResumePicked : HomeEvent
    data object RestartPicked : HomeEvent
    data object CinemaPicked : HomeEvent
    data class OutletPicked(val outlet: Outlet) : HomeEvent
    data object ScreenPicked : HomeEvent
    data object SettingsPicked : HomeEvent
    data object SettingsHeld : HomeEvent
    data class LookPicked(val look: Look) : HomeEvent
}

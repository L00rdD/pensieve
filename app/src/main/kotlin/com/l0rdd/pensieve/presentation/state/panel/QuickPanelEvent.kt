package com.l0rdd.pensieve.presentation.state.panel

import com.l0rdd.pensieve.domain.entities.scenes.Scene
import com.l0rdd.pensieve.domain.entities.system.SystemScreen

/** What the user does in the quick panel. */
sealed interface QuickPanelEvent {
    data class ScreenPicked(val screen: SystemScreen) : QuickPanelEvent
    data object CinemaPicked : QuickPanelEvent
    data object ProjectionScreenPicked : QuickPanelEvent
    data object LookCycled : QuickPanelEvent
    data class ScenePicked(val scene: Scene) : QuickPanelEvent
}

package com.l0rdd.pensieve.presentation.state.home

import com.l0rdd.pensieve.domain.entities.apps.AppId

/** A menu open over the home screen. */
sealed interface HomeMenu {
    /** Arranging an app: pin, move, hide. [favorite] tells which entries make sense. */
    data class ArrangeApp(val id: AppId, val label: String, val favorite: Boolean) : HomeMenu

    /** Picking the look. */
    data object PickLook : HomeMenu
}

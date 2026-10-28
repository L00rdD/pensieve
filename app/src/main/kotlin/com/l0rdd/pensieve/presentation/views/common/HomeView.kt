package com.l0rdd.pensieve.presentation.views.common

import android.view.View
import com.l0rdd.pensieve.presentation.state.home.HomeState

/** A look's home screen: a view tree laid out on the 1920 × 1080 stage, that shows a state. */
interface HomeView {
    val root: View

    fun render(state: HomeState)

    /** Puts the focus where the look wants it first (the main action). */
    fun focusDefault()
}

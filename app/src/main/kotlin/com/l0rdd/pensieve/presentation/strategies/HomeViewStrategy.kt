package com.l0rdd.pensieve.presentation.strategies

import android.content.Context
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.views.common.Bitmaps
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.common.HomeView
import com.l0rdd.pensieve.presentation.views.cyberpunk.CyberpunkSkin
import com.l0rdd.pensieve.presentation.views.common.ShelfHomeView
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisHomeView
import com.l0rdd.pensieve.presentation.views.marauder.MarauderSkin

/** Builds the home screen of one look. */
abstract class HomeViewStrategy {
    abstract fun execute(): HomeView
}

/** What every look's home screen is built with. */
class HomeViewTools(
    val context: Context,
    val fonts: Fonts,
    val bitmaps: Bitmaps,
    val clock: FrameClock,
    val dispatch: (HomeEvent) -> Unit,
)

/** J.A.R.V.I.S.: inside Iron Man's helmet, the arc reactor in the middle. */
class JarvisHomeViewStrategy(private val tools: HomeViewTools) : HomeViewStrategy() {
    override fun execute(): HomeView = JarvisHomeView(tools)
}

/** Cyberpunk: Netwatch's menus over Night City. */
class CyberpunkHomeViewStrategy(private val tools: HomeViewTools) : HomeViewStrategy() {
    override fun execute(): HomeView = ShelfHomeView(tools, CyberpunkSkin(tools))
}

/** The Marauder's Map, with the Gazette and Hermione's Time-Turner. */
class MarauderHomeViewStrategy(private val tools: HomeViewTools) : HomeViewStrategy() {
    override fun execute(): HomeView = ShelfHomeView(tools, MarauderSkin(tools))
}

/** Picks the strategy of a look. */
class HomeViewStrategyFactory(private val tools: HomeViewTools) {
    fun create(look: Look): HomeViewStrategy = when (look) {
        Look.JARVIS -> JarvisHomeViewStrategy(tools)
        Look.CYBERPUNK -> CyberpunkHomeViewStrategy(tools)
        Look.MARAUDER -> MarauderHomeViewStrategy(tools)
    }
}

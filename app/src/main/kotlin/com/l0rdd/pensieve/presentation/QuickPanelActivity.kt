package com.l0rdd.pensieve.presentation

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.l0rdd.pensieve.PensieveApplication
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.dependency_injector.injectMenuStyles
import com.l0rdd.pensieve.dependency_injector.injectQuickPanel
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.presentation.state.panel.QuickPanelEvent
import com.l0rdd.pensieve.presentation.state.panel.QuickPanelState
import com.l0rdd.pensieve.presentation.state.panel.QuickPanelViewModel
import com.l0rdd.pensieve.presentation.views.common.Stage
import com.l0rdd.pensieve.presentation.views.panel.PanelEntry
import com.l0rdd.pensieve.presentation.views.panel.QuickPanelView
import kotlinx.coroutines.launch

/** The quick panel over whatever is on screen, opened by the ⚙ key; BACK or ⚙ again closes it. */
class QuickPanelActivity : ComponentActivity() {
    private val viewModel: QuickPanelViewModel by viewModels { injectQuickPanel((application as PensieveApplication).scope) }
    private lateinit var view: QuickPanelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val styles = injectMenuStyles(this)
        view = QuickPanelView(this)
        setContentView(Stage(this).apply { addView(view) })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    if (state.done) { finish(); return@collect }
                    val look = state.look ?: return@collect
                    view.show(styles.create(look).execute(), getString(R.string.panel_title), entries(state, look))
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_NOTIFICATION || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
            if (event.action == KeyEvent.ACTION_UP) finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun entries(state: QuickPanelState, look: Look): List<PanelEntry> {
        val house = state.house
        val off = getString(R.string.unreachable)
        return listOf(
            PanelEntry(getString(R.string.panel_settings), "›") { viewModel.dispatch(QuickPanelEvent.ScreenPicked(SystemScreen.SETTINGS)) },
            PanelEntry(getString(R.string.panel_picture), "›") { viewModel.dispatch(QuickPanelEvent.ScreenPicked(SystemScreen.PICTURE)) },
            PanelEntry(getString(R.string.panel_keystone), "›") { viewModel.dispatch(QuickPanelEvent.ScreenPicked(SystemScreen.KEYSTONE)) },
            PanelEntry(getString(R.string.panel_network), "›") { viewModel.dispatch(QuickPanelEvent.ScreenPicked(SystemScreen.NETWORK)) },
            PanelEntry(getString(R.string.cinema), if (!house.reachable) off else if (house.isCinema) "ON" else "OFF") { viewModel.dispatch(QuickPanelEvent.CinemaPicked) },
            PanelEntry(getString(R.string.screen), when {
                !house.reachable -> off
                house.screen == null -> ""
                house.screen!!.moving != null -> getString(R.string.screen_moving)
                house.screen!!.isDown -> getString(R.string.screen_down)
                else -> getString(R.string.screen_up)
            }) { viewModel.dispatch(QuickPanelEvent.ProjectionScreenPicked) },
            PanelEntry(getString(R.string.panel_look), getString(when (look) {
                Look.JARVIS -> R.string.look_jarvis
                Look.CYBERPUNK -> R.string.look_cyberpunk
                Look.MARAUDER -> R.string.look_marauder
            })) { viewModel.dispatch(QuickPanelEvent.LookCycled) },
        )
    }
}

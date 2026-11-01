package com.l0rdd.pensieve.presentation

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.l0rdd.pensieve.PensieveApplication
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.dependency_injector.injectHome
import com.l0rdd.pensieve.dependency_injector.injectHomeViews
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.state.home.Arrange
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeMenu
import com.l0rdd.pensieve.presentation.state.home.HomeState
import com.l0rdd.pensieve.presentation.state.home.HomeViewModel
import com.l0rdd.pensieve.presentation.strategies.HomeViewStrategyFactory
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.common.HomeView
import com.l0rdd.pensieve.presentation.views.common.MenuOverlay
import com.l0rdd.pensieve.presentation.views.common.MenuStyle
import com.l0rdd.pensieve.presentation.views.common.NevaView
import com.l0rdd.pensieve.presentation.views.common.Stage
import com.l0rdd.pensieve.presentation.views.common.place
import kotlinx.coroutines.launch

/**
 * The home screen. It builds the view of the look in the state (rebuilt when the look changes),
 * shows each state on it, opens the menus, and runs the animations only while it is in front and
 * someone is there: after [REST_MS] without a key they stop, the picture staying, and the next key
 * starts them again. Left on the home screen all night, the projector has little memory and CPU to spare.
 * Neva walks by on every look, under the menus.
 */
class HomeActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels { injectHome((application as PensieveApplication).scope) }
    private val clock = FrameClock()
    private lateinit var stage: Stage
    private lateinit var layer: FrameLayout
    private lateinit var views: HomeViewStrategyFactory
    private var home: HomeView? = null
    private var look: Look? = null
    private var menu: MenuOverlay? = null
    private var shownMenu: HomeMenu? = null
    private var style: MenuStyle? = null
    private val rest = Runnable { clock.stop() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = injectHomeViews(this, clock, viewModel::dispatch)
        layer = FrameLayout(this)
        stage = Stage(this).apply { addView(layer) }
        setContentView(stage)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::show) }
        }
    }

    override fun onStart() { super.onStart(); stir() }

    override fun onStop() { stage.removeCallbacks(rest); clock.stop(); super.onStop() }

    /** Someone is there: the animations run, until [REST_MS] without a key. */
    private fun stir() {
        clock.start()
        stage.removeCallbacks(rest)
        stage.postDelayed(rest, REST_MS)
    }

    /** The home screen is home: BACK closes a menu, and otherwise does nothing. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (shownMenu != null) viewModel.dispatch(HomeEvent.MenuClosed)
    }

    /**
     * The remote's own keys, which the Google TV home used to answer: ⚙ opens the settings, ≡ opens
     * the menu of what has the focus (as OK held does).
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) Log.d(TAG, "key ${event.keyCode} ${KeyEvent.keyCodeToString(event.keyCode)}")
        if (event.action == KeyEvent.ACTION_DOWN) stir()
        when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> if (shownMenu == null) return true
            KeyEvent.KEYCODE_SETTINGS -> {
                if (event.action == KeyEvent.ACTION_UP) viewModel.dispatch(HomeEvent.SettingsPicked)
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (event.action == KeyEvent.ACTION_UP && shownMenu == null) currentFocus?.let { if (it.isLongClickable) it.performLongClick() }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun show(state: HomeState) {
        val wanted = state.look ?: return
        if (wanted != look) {
            look = wanted
            clock.clear()
            layer.removeAllViews()
            home = views.create(wanted).execute().also {
                layer.addView(it.root)
                layer.place(NevaView(this, clock), 0, NevaView.FEET - NevaView.H, NevaView.W, NevaView.H)
                it.render(state)
                it.root.post { it.focusDefault() }
            }
            style = (home as? MenuStyled)?.menuStyle
            shownMenu = null; menu = null
        } else {
            home?.render(state)
        }
        showMenu(state.menu)
    }

    private fun showMenu(wanted: HomeMenu?) {
        if (wanted == shownMenu) return
        shownMenu = wanted
        val focused = currentFocus
        menu?.let { layer.removeView(it) }
        menu = null
        wanted ?: return
        val overlay = MenuOverlay(this, style ?: return, onClose = { viewModel.dispatch(HomeEvent.MenuClosed) })
        layer.addView(overlay, FrameLayout.LayoutParams(Stage.WIDTH, Stage.HEIGHT))
        menu = overlay
        when (wanted) {
            is HomeMenu.ArrangeApp -> overlay.show(wanted.label, buildList {
                add(getString(if (wanted.favorite) R.string.arrange_unpin else R.string.arrange_pin) to { viewModel.dispatch(HomeEvent.Arranged(wanted.id, Arrange.PIN)) })
                if (wanted.favorite) {
                    add(getString(R.string.arrange_earlier) to { viewModel.dispatch(HomeEvent.Arranged(wanted.id, Arrange.EARLIER)) })
                    add(getString(R.string.arrange_later) to { viewModel.dispatch(HomeEvent.Arranged(wanted.id, Arrange.LATER)) })
                }
                add(getString(R.string.arrange_hide) to { viewModel.dispatch(HomeEvent.Arranged(wanted.id, Arrange.HIDE)) })
                add(getString(R.string.arrange_cancel) to { viewModel.dispatch(HomeEvent.MenuClosed) })
            })
            HomeMenu.PickLook -> overlay.show(getString(R.string.look_title), listOf(
                getString(R.string.look_jarvis) to { viewModel.dispatch(HomeEvent.LookPicked(Look.JARVIS)) },
                getString(R.string.look_cyberpunk) to { viewModel.dispatch(HomeEvent.LookPicked(Look.CYBERPUNK)) },
                getString(R.string.look_marauder) to { viewModel.dispatch(HomeEvent.LookPicked(Look.MARAUDER)) },
            ))
        }
        overlay.tag = focused
    }
}

private const val TAG = "Pensieve"
private const val REST_MS = 10 * 60_000L

/** A home view that dresses its menus. */
interface MenuStyled {
    val menuStyle: MenuStyle
}

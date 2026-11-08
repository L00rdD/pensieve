package com.l0rdd.pensieve.presentation

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
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
import com.l0rdd.pensieve.presentation.views.common.ScreenControl
import com.l0rdd.pensieve.presentation.views.common.ScreenHost
import com.l0rdd.pensieve.presentation.views.common.ScreenPanel
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
    private var menu: View? = null
    private var menuBack: View? = null
    private var screen: ScreenControl? = null
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
        if (event.action == KeyEvent.ACTION_DOWN) keyDown(event)
        return when (remoteKey(event.keyCode, event.action, menuOpen = shownMenu != null)) {
            RemoteKey.PASS -> super.dispatchKeyEvent(event)
            RemoteKey.SWALLOW -> true
            RemoteKey.SETTINGS -> true.also { viewModel.dispatch(HomeEvent.SettingsPicked) }
            RemoteKey.MENU -> true.also { holdFocused() }
        }
    }

    /** Someone pressed a key: it goes to the log the first time, and the animations wake up. */
    private fun keyDown(event: KeyEvent) {
        if (event.repeatCount == 0) Log.d(TAG, "key ${event.keyCode} ${KeyEvent.keyCodeToString(event.keyCode)}")
        stir()
    }

    /** ≡ does what OK held does on what has the focus. */
    private fun holdFocused() {
        currentFocus?.let { if (it.isLongClickable) it.performLongClick() }
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
            shownMenu = null; menu = null; menuBack = null; screen = null
        } else {
            home?.render(state)
        }
        showMenu(state.menu)
        (menu as? ScreenPanel)?.show(state.house)
        screen?.show(state.house)
    }

    /** Opens the menu in the state, over what had the focus, which gets it back when the menu closes. */
    private fun showMenu(wanted: HomeMenu?) {
        if (wanted == shownMenu) return
        shownMenu = wanted
        val focused = currentFocus
        foldMenu()
        val look = style ?: return
        if (wanted == null) return
        menu = if (wanted == HomeMenu.CommandScreen) commandScreen(look, focused) else listMenu(look, wanted)
        menuBack = focused
    }

    /** Takes the open menu away, and gives the focus back to what had it. */
    private fun foldMenu() {
        val back = menuBack
        menu?.let { layer.removeView(it) }
        menu = null; menuBack = null
        if (screen == null) back?.requestFocus() else foldScreen(back)
    }

    /** The look's own screen panel vanishes; its home puts the focus back on the screen's row, or else [back] has it. */
    private fun foldScreen(back: View?) {
        screen = null
        (home as? ScreenHost)?.closeScreen { if (currentFocus == null) back?.requestFocus() }
    }

    /** The look lays its own screen panel when it has one ([ScreenHost]); otherwise [ScreenPanel] unfolds over [tile]. */
    private fun commandScreen(look: MenuStyle, tile: View?): View? {
        screen = (home as? ScreenHost)?.openScreen({ viewModel.dispatch(HomeEvent.ScreenCommanded(it)) }, { viewModel.dispatch(HomeEvent.MenuClosed) })
        return if (screen == null) screenPanel(look, tile) else null
    }

    private fun screenPanel(look: MenuStyle, tile: View?) =
        ScreenPanel(this, look, { viewModel.dispatch(HomeEvent.ScreenCommanded(it)) }, { viewModel.dispatch(HomeEvent.MenuClosed) }).also {
            layer.addView(it, FrameLayout.LayoutParams(Stage.WIDTH, Stage.HEIGHT))
            it.unfold(tile)
        }

    private fun listMenu(look: MenuStyle, wanted: HomeMenu) =
        MenuOverlay(this, look, onClose = { viewModel.dispatch(HomeEvent.MenuClosed) }).also {
            layer.addView(it, FrameLayout.LayoutParams(Stage.WIDTH, Stage.HEIGHT))
            it.show(menuTitle(wanted), menuEntries(wanted).map { (text, event) -> getString(text) to { viewModel.dispatch(event) } })
        }

    private fun menuTitle(menu: HomeMenu): String = (menu as? HomeMenu.ArrangeApp)?.label ?: getString(R.string.look_title)
}

/** What the home screen does with a key of the remote. */
internal enum class RemoteKey {
    /** Not the home screen's: the views answer it. */
    PASS,

    /** The home screen's, and nothing to do. */
    SWALLOW,

    /** ⚙ released: the settings. */
    SETTINGS,

    /** ≡ released with no menu open: the menu of what has the focus. */
    MENU,
}

/**
 * BACK does nothing on the home screen (it closes a menu when one is open); ⚙ opens the settings
 * and ≡ the menu of what has the focus, both when released.
 */
internal fun remoteKey(keyCode: Int, action: Int, menuOpen: Boolean): RemoteKey = when (keyCode) {
    KeyEvent.KEYCODE_BACK -> if (menuOpen) RemoteKey.PASS else RemoteKey.SWALLOW
    KeyEvent.KEYCODE_SETTINGS -> if (action == KeyEvent.ACTION_UP) RemoteKey.SETTINGS else RemoteKey.SWALLOW
    KeyEvent.KEYCODE_MENU -> if (action == KeyEvent.ACTION_UP && !menuOpen) RemoteKey.MENU else RemoteKey.SWALLOW
    else -> RemoteKey.PASS
}

/** A menu's entries: the text of each and the event it sends. Moving makes sense for favourites only. */
internal fun menuEntries(menu: HomeMenu): List<Pair<Int, HomeEvent>> = when (menu) {
    is HomeMenu.ArrangeApp -> buildList {
        add((if (menu.favorite) R.string.arrange_unpin else R.string.arrange_pin) to HomeEvent.Arranged(menu.id, Arrange.PIN))
        if (menu.favorite) {
            add(R.string.arrange_earlier to HomeEvent.Arranged(menu.id, Arrange.EARLIER))
            add(R.string.arrange_later to HomeEvent.Arranged(menu.id, Arrange.LATER))
        }
        add(R.string.arrange_hide to HomeEvent.Arranged(menu.id, Arrange.HIDE))
        add(R.string.arrange_cancel to HomeEvent.MenuClosed)
    }
    HomeMenu.PickLook -> listOf(
        R.string.look_jarvis to HomeEvent.LookPicked(Look.JARVIS),
        R.string.look_cyberpunk to HomeEvent.LookPicked(Look.CYBERPUNK),
        R.string.look_marauder to HomeEvent.LookPicked(Look.MARAUDER),
    )
    HomeMenu.CommandScreen -> emptyList()   // a panel of its own: ScreenPanel
}

private const val TAG = "Pensieve"
private const val REST_MS = 10 * 60_000L

/** A home view that dresses its menus. */
interface MenuStyled {
    val menuStyle: MenuStyle
}

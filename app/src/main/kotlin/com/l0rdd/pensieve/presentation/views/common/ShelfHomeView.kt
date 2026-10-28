package com.l0rdd.pensieve.presentation.views.common

import android.graphics.Rect
import android.text.Html
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.apps.AppKind
import com.l0rdd.pensieve.domain.entities.apps.ShelvedApp
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.presentation.MenuStyled
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeState
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools

/**
 * The shelf layout, dressed by a look's [skin]: the header with the settings always at hand,
 * the house on the left under the clock, and on the right, scrolling, what to resume, the
 * favourites (5 a row) and every other app (6 a row). The bottom line says what OK does.
 */
class ShelfHomeView(private val tools: HomeViewTools, private val skin: ShelfSkin) : HomeView, MenuStyled {
    private val context = tools.context
    private val dispatch = tools.dispatch
    override val root = FrameLayout(context)
    override val menuStyle get() = skin.menuStyle

    private val line = skin.line()
    private val time = skin.time()
    private val gear = skin.gear()
    private val clock = skin.clock()
    private val hero = skin.hero()
    private val house = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val favorites = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val others = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val hint = skin.hint()
    private val hints = HashMap<View, () -> CharSequence>()
    private var appsKey: List<Any>? = null
    private var houseKey: List<Any>? = null
    private var houseTiles = emptyList<ActionTile>()
    private var state = HomeState.INITIAL

    init {
        root.addView(skin.background(), FrameLayout.LayoutParams(-1, -1))
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            addView(skin.brand())
            addView(line, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = 28 })
            addView(time, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = 22 })
            addView(gear)
        }
        root.place(header, 60, 18, 1800, 64)
        val rail = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(clock.view, LinearLayout.LayoutParams(-1, clock.height))
            addView(skin.label(context.getString(R.string.house), null), LinearLayout.LayoutParams(-1, -2).apply { topMargin = 10; bottomMargin = 14 })
            addView(house)
        }
        root.place(rail, 60, 108, 410, 900)
        val page = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 60, 90)
            addView(skin.label(context.getString(R.string.resume), null), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 16 })
            addView(hero.view, LinearLayout.LayoutParams(-1, 318).apply { bottomMargin = 30 })
            addView(skin.label(context.getString(R.string.favorites), context.getString(R.string.favorites_hint)), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 16 })
            addView(favorites, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 30 })
            addView(skin.label(context.getString(R.string.all_apps), null), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 16 })
            addView(others)
        }
        val scroll = ScrollView(context).apply { isVerticalScrollBarEnabled = false; isFillViewport = true; clipToPadding = false; addView(page) }
        root.place(scroll, 510, 92, 1410, 928)
        root.place(hint, 60, 1028, 1800, 40)
        skin.overlay()?.let { root.addView(it, FrameLayout.LayoutParams(-1, -1)) }

        hero.resume.pressable({ dispatch(HomeEvent.ResumePicked) })
        hero.restart.pressable({ dispatch(HomeEvent.RestartPicked) })
        gear.pressable({ dispatch(HomeEvent.SettingsPicked) }, { dispatch(HomeEvent.SettingsHeld) })
        hints[hero.resume] = { html(context.getString(R.string.hint_resume, state.resume?.let { FrenchTime.clock(it.position.inWholeMinutes) } ?: "")) }
        hints[hero.restart] = { html(context.getString(R.string.hint_restart)) }
        hints[gear] = { html(context.getString(R.string.hint_settings)) }

        var last: Rect? = null
        root.viewTreeObserver.addOnGlobalFocusChangeListener { _, focused ->
            focused ?: return@addOnGlobalFocusChangeListener
            hint.text = hints[focused]?.invoke() ?: ""
            val rect = Rect().also { focused.getDrawingRect(it); root.offsetDescendantRectToMyCoords(focused, it) }
            skin.onFocusMove(last, rect)
            last = rect
        }
    }

    override fun focusDefault() {
        (if (state.resume != null) hero.resume else favorites.getFocusables(View.FOCUS_FORWARD).firstOrNull() ?: gear).requestFocus()
    }

    override fun render(state: HomeState) {
        this.state = state
        state.now?.let { clock.show(it); time.text = FrenchTime.hm(it) }
        hero.show(state.resume, tools.bitmaps.of(state.poster), state.now)
        hero.restart.visibility = if (state.resume == null) View.GONE else View.VISIBLE
        hero.resume.visibility = hero.restart.visibility
        showApps(state)
        showHouse(state.house)
        root.findFocus()?.let { f -> hints[f]?.let { hint.text = it() } }
    }

    private fun showApps(state: HomeState) {
        val key = (state.apps.favorites + listOf(null) + state.apps.others).map { s -> s?.let { listOf(it.app.id, it.app.label, it.isNew, it.app.artwork?.key ?: "") } ?: "|" }
        if (key == appsKey) return
        appsKey = key
        val focused = root.findFocus()?.getTag(R.id.app_id) as? String
        grid(favorites, state.apps.favorites, 5, 176, skin::favoriteTile, focused)
        grid(others, state.apps.others, 6, 128, skin::appTile, focused)
    }

    private fun grid(into: LinearLayout, apps: List<ShelvedApp>, columns: Int, height: Int, make: () -> AppTile, focused: String?) {
        into.removeAllViews()
        apps.chunked(columns).forEachIndexed { r, row ->
            val line = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEachIndexed { c, shelved ->
                val tile = make()
                tile.show(shelved, tools.bitmaps.of(shelved.app.artwork), r * columns + c)
                tile.view.setTag(R.id.app_id, shelved.app.id.value)
                tile.view.pressable({ dispatch(HomeEvent.AppPicked(shelved.app.id)) }, { dispatch(HomeEvent.AppHeld(shelved.app.id)) })
                hints[tile.view] = { html(context.getString(if (shelved.app.kind == AppKind.STORE) R.string.hint_store else R.string.hint_open)) }
                line.addView(tile.view, LinearLayout.LayoutParams(0, height, 1f).apply { if (c < columns - 1) rightMargin = 20 })
                if (shelved.app.id.value == focused) tile.view.post { tile.view.requestFocus() }
            }
            repeat(columns - row.size) { line.addView(View(context), LinearLayout.LayoutParams(0, height, 1f).apply { if (it < columns - row.size - 1) rightMargin = 20 }) }
            into.addView(line, LinearLayout.LayoutParams(-1, height).apply { if (r > 0) topMargin = 22 })
        }
    }

    private fun showHouse(house: House) {
        val key = listOf<Any>(house.outlets.map { it.id }, house.screen != null)
        if (key != houseKey) {
            houseKey = key
            this.house.removeAllViews()
            houseTiles = List(1 + house.outlets.size + (if (house.screen != null) 1 else 0)) { skin.action() }
            houseTiles.forEach { this.house.addView(it.view, LinearLayout.LayoutParams(-1, 84).apply { bottomMargin = 14 }) }
        }
        val off = context.getString(R.string.unreachable)
        houseTiles[0].show(context.getString(R.string.cinema), context.getString(R.string.cinema_sub), if (!house.reachable) off else if (house.isCinema) "ON" else "", "◐")
        houseTiles[0].view.pressable({ dispatch(HomeEvent.CinemaPicked) })
        hints[houseTiles[0].view] = { html(context.getString(R.string.hint_cinema)) }
        house.outlets.forEachIndexed { i, outlet ->
            val tile = houseTiles[1 + i]
            tile.show(outlet.name, context.getString(if (outlet.on) R.string.on else R.string.off), if (!house.reachable) off else if (outlet.on) "ON" else "OFF", "✦")
            tile.view.pressable({ dispatch(HomeEvent.OutletPicked(outlet)) })
            hints[tile.view] = { html(context.getString(R.string.hint_outlet)) }
        }
        house.screen?.let { screen ->
            val tile = houseTiles.last()
            tile.show(context.getString(R.string.screen), context.getString(R.string.screen_sub),
                if (!house.reachable) off else if (screen.moving != null) context.getString(R.string.screen_moving) else context.getString(if (screen.isDown) R.string.screen_down else R.string.screen_up), "▤")
            tile.view.pressable({ dispatch(HomeEvent.ScreenPicked) })
            hints[tile.view] = { html(context.getString(R.string.hint_screen)) }
        }
    }

    private fun html(text: String): CharSequence = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
}

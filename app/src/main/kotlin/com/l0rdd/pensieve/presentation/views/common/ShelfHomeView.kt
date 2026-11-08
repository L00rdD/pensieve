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
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand
import com.l0rdd.pensieve.presentation.MenuStyled
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeState
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools

/**
 * The shelf layout, dressed by a look's [skin]: the header with the settings always at hand,
 * the house on the left under the clock (or the look's own [HouseColumn]), and on the right,
 * scrolling, what to resume, the favourites (5 a row) and every other app (6 a row). The bottom
 * line says what OK does, or what a look's panel says ([Quill]). A look's own screen panel is
 * laid in its house column ([ScreenHost]).
 */
class ShelfHomeView(private val tools: HomeViewTools, private val skin: ShelfSkin) : HomeView, MenuStyled, ScreenHost {
    private val context = tools.context
    private val dispatch = tools.dispatch
    override val root = FrameLayout(context)
    override val menuStyle get() = skin.menuStyle

    private val line = skin.line()
    private val time = skin.time()
    private val gear = skin.gear()
    private val column = skin.house()
    private val clock = if (column == null) skin.clock() else null
    private val hero = skin.hero()
    private val house = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val favorites = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val others = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val hint = skin.hint()
    private val hints = HashMap<View, () -> CharSequence>()
    private val quill = Quill(hint, skin.menuStyle.accent) { focusedHint() ?: "" }
    private var wiredRows = emptyList<View>()
    private var screen: ScreenControl? = null
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
        placeRail()
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
            showHint(hints[focused]?.invoke() ?: "")
            // a menu open over the page holds the focus outside it: no footsteps then
            if (!focused.isDescendantOf(root)) { last = null; return@addOnGlobalFocusChangeListener }
            val rect = Rect().also { focused.getDrawingRect(it); root.offsetDescendantRectToMyCoords(focused, it) }
            skin.onFocusMove(last, rect)
            last = rect
        }
    }

    /** The look's house column at the top left, or the clock, the house's label and its tiles under it. */
    private fun placeRail() {
        column?.let { root.place(it.view, 60, 90, 410, 918); return }
        val clock = clock ?: return
        val rail = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(clock.view, LinearLayout.LayoutParams(-1, clock.height))
            addView(skin.label(context.getString(R.string.house), null), LinearLayout.LayoutParams(-1, -2).apply { topMargin = 10; bottomMargin = 14 })
            addView(house)
        }
        root.place(rail, 60, 108, 410, 900)
    }

    override fun focusDefault() {
        (if (state.resume != null) hero.resume else favorites.getFocusables(View.FOCUS_FORWARD).firstOrNull() ?: gear).requestFocus()
    }

    override fun render(state: HomeState) {
        this.state = state
        state.now?.let(::showTime)
        hero.show(state.resume, tools.bitmaps.of(state.poster), state.now)
        hero.restart.visibility = if (state.resume == null) View.GONE else View.VISIBLE
        hero.resume.visibility = hero.restart.visibility
        showApps(state)
        if (column != null) showColumn(column, state.house) else showHouse(state.house)
        focusedHint()?.let(::showHint)
    }

    private fun showTime(now: java.time.LocalDateTime) {
        clock?.show(now)
        column?.showTime(now)
        time.text = FrenchTime.hm(now)
    }

    /** What OK does on what has the focus, if it says. */
    private fun focusedHint(): CharSequence? = root.findFocus()?.let { hints[it] }?.invoke()

    /** Writes [text] on the hint line, unless the quill is writing there. */
    private fun showHint(text: CharSequence) {
        if (!quill.writing) hint.text = text
    }

    /** The look's house column, its rows wired again when they were made again. */
    private fun showColumn(column: HouseColumn, house: House) {
        column.show(house)
        if (column.rows == wiredRows) return
        wiredRows.forEach { hints.remove(it) }
        wiredRows = column.rows
        val things = houseThings(house)
        column.rows.forEachIndexed { i, row -> wire(row, things[i], i) }
    }

    /** OK on the [index]th row does what its [thing] does (a presence's does nothing); its hint is read when it has the focus. */
    private fun wire(row: View, thing: HouseThing, index: Int) {
        when (thing) {
            HouseThing.Cinema -> row.pressable({ dispatch(HomeEvent.CinemaPicked) })
            is HouseThing.Plug -> thing.outlet.id.let { id -> row.pressable({ outlet(id)?.let { dispatch(HomeEvent.OutletPicked(it)) } }) }
            is HouseThing.Projection -> row.pressable({ dispatch(HomeEvent.ScreenPicked) })
            is HouseThing.Someone -> row.isFocusable = true
        }
        hints[row] = { houseThings(state.house).getOrNull(index)?.let { html(context.resources.say(hintOf(it, state.house))) } ?: "" }
    }

    /** The outlet [id] as it is now. */
    private fun outlet(id: OutletId) = state.house.outlets.firstOrNull { it.id == id }

    override fun openScreen(onCommand: (ScreenCommand) -> Unit, onClose: () -> Unit): ScreenControl? {
        val column = column ?: return null
        val control = skin.screenControl(onCommand, onClose, quill) ?: return null
        screen = control
        control.show(state.house)
        column.cover(control.view) { control.reveal() }
        return control
    }

    override fun closeScreen(then: () -> Unit) {
        val control = screen ?: return then()
        screen = null
        control.vanish { column?.uncover(control.view) { screenRow()?.requestFocus(); then() } }
    }

    /** The screen's row of the house column, if there is one. */
    private fun screenRow(): View? = column?.rows?.getOrNull(houseThings(state.house).indexOfFirst { it is HouseThing.Projection })

    private fun showApps(state: HomeState) {
        val key = (state.apps.favorites + listOf(null) + state.apps.others).map { s -> s?.let { listOf(it.app.id, it.app.label, it.isNew, it.app.artwork?.key ?: "") } ?: "|" }
        if (key == appsKey) return
        appsKey = key
        val focused = root.findFocus()?.getTag(R.id.app_id) as? String
        grid(Grid(favorites, 5, 176, skin::favoriteTile), state.apps.favorites, focused)
        grid(Grid(others, 6, 128, skin::appTile), state.apps.others, focused)
    }

    /** A grid of app tiles: where it goes, how many a row, how high, and how a tile is made. */
    private class Grid(val into: LinearLayout, val columns: Int, val height: Int, val make: () -> AppTile)

    /** Fills [grid] with [apps], row by row; the app that had the focus ([focused]) gets it back. */
    private fun grid(grid: Grid, apps: List<ShelvedApp>, focused: String?) {
        grid.into.removeAllViews()
        apps.chunked(grid.columns).forEachIndexed { r, row ->
            val line = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEachIndexed { c, shelved ->
                val view = tile(grid.make, shelved, r * grid.columns + c)
                line.addView(view, cell(grid.height, spaced = c < grid.columns - 1))
                if (shelved.app.id.value == focused) view.post { view.requestFocus() }
            }
            repeat(grid.columns - row.size) { line.addView(View(context), cell(grid.height, spaced = it < grid.columns - row.size - 1)) }
            grid.into.addView(line, LinearLayout.LayoutParams(-1, grid.height).apply { if (r > 0) topMargin = 22 })
        }
    }

    /** The tile of one app, the [index]th of its grid: OK opens it, OK held arranges it. */
    private fun tile(make: () -> AppTile, shelved: ShelvedApp, index: Int): View {
        val tile = make()
        tile.show(shelved, tools.bitmaps.of(shelved.app.artwork), index)
        tile.view.setTag(R.id.app_id, shelved.app.id.value)
        tile.view.pressable({ dispatch(HomeEvent.AppPicked(shelved.app.id)) }, { dispatch(HomeEvent.AppHeld(shelved.app.id)) })
        hints[tile.view] = { html(context.getString(if (shelved.app.kind == AppKind.STORE) R.string.hint_store else R.string.hint_open)) }
        return tile.view
    }

    /** A cell of a grid's row, [spaced] from the next one. */
    private fun cell(height: Int, spaced: Boolean) = LinearLayout.LayoutParams(0, height, 1f).apply { if (spaced) rightMargin = 20 }

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
        screenState(house)?.let { state ->
            val tile = houseTiles.last()
            tile.show(context.getString(R.string.screen), context.getString(R.string.screen_sub), context.getString(state), "▤")
            tile.view.pressable({ dispatch(HomeEvent.ScreenPicked) })
            hints[tile.view] = { html(context.getString(R.string.hint_screen)) }
        }
    }

    private fun html(text: String): CharSequence = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)

    private fun View.isDescendantOf(ancestor: View): Boolean {
        var parent = parent
        while (parent is View) { if (parent === ancestor) return true; parent = parent.parent }
        return false
    }
}

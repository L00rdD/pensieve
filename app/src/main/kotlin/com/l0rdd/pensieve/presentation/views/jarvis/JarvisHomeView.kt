package com.l0rdd.pensieve.presentation.views.jarvis

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.apps.AppKind
import com.l0rdd.pensieve.domain.entities.apps.ShelvedApp
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Outlet
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.presentation.MenuStyled
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeState
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools
import com.l0rdd.pensieve.presentation.views.common.FrenchTime
import com.l0rdd.pensieve.presentation.views.common.HomeView
import com.l0rdd.pensieve.presentation.views.common.MenuStyle
import com.l0rdd.pensieve.presentation.views.common.place
import com.l0rdd.pensieve.presentation.views.common.pressable

/**
 * J.A.R.V.I.S.'s home screen, inside the helmet: the arc reactor resumes what was left on
 * Jellyfin, six favourites are modules round it, every other app is in the strip below, and the
 * house is further down (the helmet looks down when the focus goes there). J.A.R.V.I.S. comments
 * on what has the focus.
 */
class JarvisHomeView(private val tools: HomeViewTools) : HomeView, MenuStyled {
    private val context = tools.context
    private val fonts = tools.fonts
    private val dispatch = tools.dispatch
    override val root = FrameLayout(context)
    override val menuStyle = MenuStyle(fonts.rajdhaniSemi, JarvisColors.TEXT, JarvisColors.GOLD, 0xF2041019.toInt(), 0xB3000000.toInt(), 30f)

    private val page = FrameLayout(context)
    private val status = text(fonts.rajdhaniSemi, 19f, JarvisColors.MUTED, 0.2f)
    private val target = text(fonts.rajdhaniSemi, 16f, JarvisColors.MUTED, 0.3f).apply { gravity = Gravity.CENTER }
    private val reactor = ReactorView(context, fonts, tools.clock, context.getString(R.string.jv_engraving))
    private val restart = JarvisButton(context, fonts.rajdhaniBold, "↺ " + context.getString(R.string.restart).uppercase(), 16f)
    private val gear = JarvisButton(context, fonts.rajdhaniBold, "⚙ " + context.getString(R.string.settings).uppercase(), 17f)
    private val modules = List(6) { JarvisModuleView(context, fonts, tools.clock, left = it < 3) }
    private val callouts = List(6) { CalloutView(context, dotAtRight = it < 3) }
    private val strip = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; setPadding(8, 14, 8, 14) }
    private val homeRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 10, 0, 10) }
    private val more = text(fonts.rajdhaniSemi, 15f, JarvisColors.MUTED, 0.35f).apply { gravity = Gravity.CENTER }
    private val voice = JarvisVoiceView(context, fonts, tools.clock)
    private val says = HashMap<View, () -> Spanned>()
    private val houseViews = mutableSetOf<View>()
    private var stripKey: List<Any>? = null
    private var houseKey: List<Any>? = null
    private var state: HomeState = HomeState.INITIAL

    init {
        root.addView(HelmetView(context), FrameLayout.LayoutParams(-1, -1))
        root.addView(page, FrameLayout.LayoutParams(-1, -1))
        // the page fades out under the top of the helmet when it moves up
        root.addView(object : View(context) {
            private val fade = Paint()
            override fun onDraw(canvas: Canvas) {
                fade.shader = LinearGradient(0f, 0f, 0f, 215f, intArrayOf(JarvisColors.BG, JarvisColors.BG, 0), floatArrayOf(0f, 0.62f, 1f), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), 215f, fade)
            }
        }.also { it.alpha = 0f }, FrameLayout.LayoutParams(-1, 215))
        root.place(text(fonts.rajdhaniSemi, 16f, JarvisColors.MUTED, 0.35f).apply { text = context.getString(R.string.jv_company) }, 70, 40)
        root.place(text(fonts.michroma, 22f, JarvisColors.CYAN, 0.3f).apply { text = context.getString(R.string.jv_room); setShadowLayer(14f, 0f, 0f, JarvisColors.alpha(JarvisColors.CYAN, 0.5f)) }, 70, 64)
        root.place(status, 900, 42, 720, 40).also { status.gravity = Gravity.END }
        root.place(gear, 1648, 34)
        root.place(CompassView(context, fonts.rajdhaniSemi, tools.clock), 660, 104, 600, 40)
        root.place(LadderView(context, fonts.rajdhaniSemi, tools.clock, "ALT", "0247", 2.3f), 26, 360, 60, 380)
        root.place(LadderView(context, fonts.rajdhaniSemi, tools.clock, "THR", "100%", -1.5f), 1834, 360, 60, 380)

        page.place(target, 460, 160, 1000, 30)
        page.place(reactor, 775, 252, 370, 370)
        page.place(restart, 840, 664, 240, 40)
        val rows = listOf(196, 366, 536)
        modules.forEachIndexed { i, m ->
            val left = i < 3
            page.place(m, if (left) 250 else 1270, rows[i % 3], 400, 132)
            page.place(callouts[i], if (left) 650 else 1200, rows[i % 3] + 60, 70, 12)
        }
        page.place(label(context.getString(R.string.applications).uppercase(), "05"), 230, 742, 1460, 26)
        val scroller = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false; clipToPadding = false; addView(strip) }
        page.place(scroller, 222, 768, 1476, 150)
        page.place(label(context.getString(R.string.jv_systems), "06"), 230, 1000, 1460, 26)
        page.place(homeRow, 230, 1030, 1460, 180)
        root.place(more, 660, 958, 600, 26)
        root.place(voice, 410, 1000, 1200, 44)

        reactor.pressable({ dispatch(if (state.resume != null) HomeEvent.ResumePicked else jellyfin()) })
        restart.pressable({ dispatch(HomeEvent.RestartPicked) })
        gear.pressable({ dispatch(HomeEvent.SettingsPicked) }, { dispatch(HomeEvent.SettingsHeld) })
        says[reactor] = { state.resume?.let(::sayResume) ?: html(context.getString(R.string.jv_say_nothing)) }
        says[restart] = { html(context.getString(R.string.hint_restart)) }
        says[gear] = { html(context.getString(R.string.jv_say_settings)) }

        root.viewTreeObserver.addOnGlobalFocusChangeListener { _, focused ->
            focused ?: return@addOnGlobalFocusChangeListener
            says[focused]?.let { voice.say(it()) }
            val down = focused in houseViews
            page.animate().translationY(if (down) -350f else 0f).setDuration(480).start()
            root.getChildAt(2).animate().alpha(if (down) 1f else 0f).setDuration(480).start()
            more.animate().alpha(if (down) 0f else 1f).setDuration(300).start()
        }
    }

    override fun focusDefault() { reactor.requestFocus() }

    override fun render(state: HomeState) {
        this.state = state
        status.text = statusLine(state)
        showResume(state.resume)
        val favorites = state.apps.favorites.take(6)
        modules.forEachIndexed { i, m ->
            val app = favorites.getOrNull(i)
            m.visibility = if (app == null) View.INVISIBLE else View.VISIBLE
            callouts[i].visibility = m.visibility
            app ?: return@forEachIndexed
            m.show("%02d".format(i + 1), app.app.label, subtitle(app), tools.bitmaps.of(app.app.artwork))
            m.pressable({ dispatch(HomeEvent.AppPicked(app.app.id)) }, { dispatch(HomeEvent.AppHeld(app.app.id)) })
            says[m] = { sayApp(app) }
        }
        showStrip(state.apps.favorites.drop(6) + state.apps.others)
        showHouse(state.house)
        root.findFocus()?.let { f -> says[f]?.let { voice.say(it()) } }
    }

    private fun showResume(resume: Resume?) {
        if (resume == null) {
            reactor.show("JELLYFIN", "▶ " + context.getString(R.string.open_jellyfin).substringBefore(' ').uppercase(), context.getString(R.string.nothing_to_resume).uppercase(), 0f)
            target.text = ""
            restart.visibility = View.INVISIBLE
            return
        }
        val watched = resume.position.inWholeMinutes; val total = resume.runtime.inWholeMinutes
        reactor.show(resume.title.uppercase(), "▶ " + context.getString(R.string.resume).uppercase(), "${FrenchTime.clock(watched)} / ${FrenchTime.clock(total)} · ${(resume.progress * 100).toInt()}%", resume.progress.toFloat())
        target.text = SpannableStringBuilder("CIBLE // ").apply {
            val start = length; append(listOfNotNull(resume.series, resume.title).joinToString(" · ").uppercase())
            setSpan(ForegroundColorSpan(JarvisColors.GOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(listOfNotNull(resume.year?.toString(), resume.genres.firstOrNull()?.uppercase()).joinToString("") { " // $it" })
        }
        restart.visibility = View.VISIBLE
    }

    private fun showStrip(apps: List<ShelvedApp>) {
        val key = apps.map { listOf(it.app.id, it.app.label, it.isNew, it.app.artwork?.key ?: "") }
        if (key == stripKey) return
        stripKey = key
        val focusedId = (strip.findFocus()?.tag as? String)
        strip.removeAllViews()
        apps.forEach { shelved ->
            val tile = JarvisAppView(context, fonts)
            tile.tag = shelved.app.id.value
            tile.show(shelved.app.label, tools.bitmaps.of(shelved.app.artwork), shelved.isNew, if (shelved.app.artwork == null && shelved.app.kind == AppKind.STORE) "+" else null)
            tile.pressable({ dispatch(HomeEvent.AppPicked(shelved.app.id)) }, { dispatch(HomeEvent.AppHeld(shelved.app.id)) })
            says[tile] = { sayApp(shelved) }
            strip.addView(tile, LinearLayout.LayoutParams(148, 118).apply { rightMargin = 14 })
            if (shelved.app.id.value == focusedId) tile.post { tile.requestFocus() }
        }
    }

    private fun showHouse(house: House) {
        val key = listOf<Any>(house.outlets.map { it.id }, house.screen != null)
        if (key != houseKey) {
            houseKey = key
            homeRow.removeAllViews(); houseViews.clear()
            val count = 1 + house.outlets.size + (if (house.screen != null) 1 else 0)
            val width = (1460 - 24 * (count - 1)) / count
            repeat(count) { homeRow.addView(JarvisSystemView(context, fonts).also { houseViews += it }, LinearLayout.LayoutParams(width, 150).apply { if (it < count - 1) rightMargin = 24 }) }
            more.text = context.getString(R.string.jv_more, count)
        }
        val views = (0 until homeRow.childCount).map { homeRow.getChildAt(it) as JarvisSystemView }
        val off = context.getString(R.string.unreachable)
        views[0].show("SCÈNE", context.getString(R.string.cinema), context.getString(R.string.cinema_sub), if (!house.reachable) off else if (house.isCinema) "ACTIF" else "PRÊT", "◐")
        views[0].pressable({ dispatch(HomeEvent.CinemaPicked) })
        says[views[0]] = { html(context.getString(R.string.jv_say_cinema)) }
        house.outlets.forEachIndexed { i, outlet -> showOutlet(views[1 + i], outlet, house.reachable) }
        house.screen?.let { screen ->
            val v = views.last()
            val where = context.getString(if (screen.isDown) R.string.down else R.string.up)
            v.show("ÉCRAN", context.getString(R.string.screen_sub), "position ${screen.position} % · $where",
                if (!house.reachable) off else if (screen.moving != null) context.getString(R.string.screen_moving) else context.getString(if (screen.isDown) R.string.screen_down else R.string.screen_up),
                "▤", screen.position / 100f)
            v.pressable({ dispatch(HomeEvent.ScreenPicked) })
            says[v] = { html(context.getString(R.string.jv_say_screen, "<b>$where</b>")) }
        }
    }

    private fun showOutlet(view: JarvisSystemView, outlet: Outlet, reachable: Boolean) {
        val on = context.getString(if (outlet.on) R.string.on else R.string.off)
        view.show("PRISE", outlet.name, on, if (!reachable) context.getString(R.string.unreachable) else if (outlet.on) "ON" else "OFF", "✦")
        view.pressable({ dispatch(HomeEvent.OutletPicked(outlet)) })
        says[view] = { html(context.getString(R.string.jv_say_outlet, "<b>${TextUtils.htmlEncode(outlet.name)}</b>", on)) }
    }

    private fun statusLine(state: HomeState): CharSequence = SpannableStringBuilder().apply {
        fun pair(label: String, value: String) {
            if (isNotEmpty()) append("    ")
            append(label).append(' ')
            val start = length; append(value)
            setSpan(ForegroundColorSpan(JarvisColors.TEXT), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val house = state.house
        if (house.reachable) {
            pair("PRISES", "%02d".format(house.outlets.count { it.on }))
            house.screen?.let { pair("ÉCRAN", if (it.isDown) "▼ BAS" else "▲ HAUT") }
        } else pair("HUB", context.getString(R.string.unreachable))
        state.now?.let { pair("HOST", FrenchTime.hm(it)) }
    }

    private fun subtitle(app: ShelvedApp) = if (app.isNew) context.getString(R.string.badge_new) else app.app.id.value.substringAfterLast('.')

    private fun sayApp(app: ShelvedApp): Spanned = html(
        if (app.isNew) context.getString(R.string.jv_say_new, "<b>${TextUtils.htmlEncode(app.app.label)}</b>")
        else if (app.app.kind == AppKind.STORE) context.getString(R.string.jv_say_store)
        else context.getString(R.string.jv_say_app, "<b>${TextUtils.htmlEncode(app.app.label)}</b>"),
    )

    private fun sayResume(resume: Resume): Spanned {
        val end = state.now?.plusSeconds(resume.remaining.inWholeSeconds)?.let { FrenchTime.hm(it) } ?: "—"
        return html(context.getString(R.string.jv_say_resume, "<b>${TextUtils.htmlEncode(resume.title)}</b>", FrenchTime.clock(resume.position.inWholeMinutes), end))
    }

    private fun jellyfin() = HomeEvent.AppPicked(com.l0rdd.pensieve.domain.entities.apps.AppId("org.jellyfin.androidtv"))

    private fun html(text: String): Spanned = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)

    private fun label(text: String, code: String) = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(text(fonts.rajdhaniSemi, 14f, JarvisColors.BG, 0.1f).apply { this.text = code; setBackgroundColor(JarvisColors.CYAN); setPadding(7, 0, 7, 0) })
        addView(text(fonts.rajdhaniSemi, 16f, JarvisColors.MUTED, 0.35f).apply { this.text = text; setPadding(16, 0, 16, 0) })
        addView(object : View(context) {
            private val p = Paint()
            override fun onDraw(canvas: Canvas) {
                p.shader = LinearGradient(0f, 0f, width.toFloat(), 0f, JarvisColors.alpha(JarvisColors.CYAN, 0.35f), 0, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, height / 2f, width.toFloat(), height / 2f + 1, p)
            }
        }, LinearLayout.LayoutParams(0, 2, 1f))
    }

    private fun text(face: android.graphics.Typeface, size: Float, color: Int, spacing: Float) = TextView(context).apply {
        typeface = face; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color); letterSpacing = spacing; includeFontPadding = false
    }
}

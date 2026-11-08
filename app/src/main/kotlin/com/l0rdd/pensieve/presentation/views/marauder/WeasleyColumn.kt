package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools
import com.l0rdd.pensieve.presentation.views.common.FrenchTime
import com.l0rdd.pensieve.presentation.views.common.HouseColumn
import com.l0rdd.pensieve.presentation.views.common.HouseThing
import com.l0rdd.pensieve.presentation.views.common.houseThings
import com.l0rdd.pensieve.presentation.views.common.say
import com.l0rdd.pensieve.presentation.views.common.stateWords
import java.time.LocalDateTime

/**
 * The Marauder's house column: the Weasleys' clock with the Time-Turner behind it, the time in
 * words, the house's [label], then the hands' legend, one focusable row per thing of the house.
 * A screen panel takes the legend's place.
 */
class WeasleyColumn(tools: HomeViewTools, label: View) : HouseColumn {
    private val context = tools.context
    private val fonts = tools.fonts
    private val clock = WeasleyClockView(context, fonts.fell, context.getString(R.string.mm_inscription), tools.clock)
    private val words = text(fonts.fellItalic, 30f, InkColors.INK, 0f)
    private val digits = text(fonts.fell, 16f, InkColors.RED, 0.2f)
    private val date = text(fonts.fellItalic, 22f, InkColors.MUTED, 0f)
    private val legend = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val slot = FrameLayout(context).apply { addView(legend, FrameLayout.LayoutParams(-1, -2)) }
    private var keys = emptyList<String>()
    private var house = House.UNKNOWN

    override var rows = emptyList<View>()
        private set

    override val view: View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(clock, LinearLayout.LayoutParams(WeasleyClockView.WIDTH, WeasleyClockView.HEIGHT))
        addView(words, LinearLayout.LayoutParams(-1, -2))
        addView(digits, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 2 })
        addView(date, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 4 })
        addView(label, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 10; bottomMargin = 14 })
        addView(slot, LinearLayout.LayoutParams(-1, -2))
    }

    override fun showTime(now: LocalDateTime) {
        clock.showTime(now)
        words.text = FrenchTime.inWords(now.hour, now.minute)
        digits.text = FrenchTime.hm(now).replace(":", " · ")
        date.text = FrenchTime.longDate(now)
    }

    override fun show(house: House) {
        this.house = house
        val things = houseThings(house)
        val keys = things.map { it.key }
        if (keys != this.keys) rebuild(keys)
        things.forEachIndexed { i, thing -> (rows[i] as LegendRow).show(thing, house) }
        clock.show(things, house)
    }

    /** New rows for [keys]; the row that had the focus keeps it if its thing is still there. */
    private fun rebuild(keys: List<String>) {
        val focused = this.keys.getOrNull(rows.indexOfFirst { it.isFocused })
        this.keys = keys
        legend.removeAllViews()
        rows = keys.indices.map { i -> LegendRow(i).also { legend.addView(it, LinearLayout.LayoutParams(-1, ROW)) } }
        rows.getOrNull(keys.indexOf(focused))?.let { row -> row.post { row.requestFocus() } }
    }

    override fun cover(panel: View, then: () -> Unit) {
        legend.animate().alpha(0f).setDuration(DISSOLVE).withEndAction {
            slot.addView(panel, FrameLayout.LayoutParams(-1, -2))
            panel.afterLayout {
                then()
                if (legend.hasFocus()) panel.requestFocus()
                legend.visibility = View.INVISIBLE
            }
        }.start()
    }

    override fun uncover(panel: View, then: () -> Unit) {
        legend.animate().cancel()
        legend.alpha = 0f
        legend.visibility = View.VISIBLE
        then()
        slot.removeView(panel)
        legend.animate().alpha(1f).setDuration(FADE_BACK).start()
    }

    /** Once, the next time this view is laid out. */
    private fun View.afterLayout(action: () -> Unit) = addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, orr: Int, ob: Int) {
            v.removeOnLayoutChangeListener(this)
            action()
        }
    })

    /** The hand's key for the [index]th thing: its engraving, its name, and in italic where it is; framed in red with the focus. */
    private inner class LegendRow(private val index: Int) : View(context) {
        private var thing: HouseThing = HouseThing.Cinema
        private var shown = House.UNKNOWN
        private val icons = WeasleyIcons()
        private val frame = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = InkColors.RED }
        private val name = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 19f }
        private val state = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 16.5f; textAlign = Paint.Align.RIGHT }

        init { isFocusable = true }

        fun show(thing: HouseThing, house: House) { this.thing = thing; shown = house; invalidate() }

        override fun onDraw(canvas: Canvas) {
            val ink = if (isFocused) InkColors.RED else InkColors.NEWS_INK
            val mid = height / 2f
            if (isFocused) canvas.drawRect(0.5f, 0.5f, width - 0.5f, height - 0.5f, frame)
            canvas.save(); canvas.translate(26f, mid + 1); icons.draw(canvas, iconOf(thing, shown), 0.95f, ink); canvas.restore()
            name.color = ink
            val title = TextUtils.ellipsize(nameOf(thing), name, NAME_MAX, TextUtils.TruncateAt.END).toString()
            canvas.drawText(title, NAME_X, mid - (name.ascent() + name.descent()) / 2, name)
            state.color = if (isFocused) InkColors.RED else InkColors.MUTED
            val room = width - STATE_RIGHT - NAME_X - name.measureText(title) - GAP
            val where = TextUtils.ellipsize(context.resources.say(stateWords(thing, shown)), state, room, TextUtils.TruncateAt.END)
            canvas.drawText(where.toString(), width - STATE_RIGHT, mid - (state.ascent() + state.descent()) / 2, state)
        }

        override fun onFocusChanged(gained: Boolean, direction: Int, previous: Rect?) {
            super.onFocusChanged(gained, direction, previous)
            if (gained) clock.picked = index else if (clock.picked == index) clock.picked = -1
            invalidate()
        }
    }

    private fun nameOf(thing: HouseThing): String = when (thing) {
        HouseThing.Cinema -> context.getString(R.string.cinema)
        is HouseThing.Plug -> thing.outlet.name
        is HouseThing.Projection -> context.getString(R.string.screen)
        is HouseThing.Someone -> thing.presence.name
    }

    private fun text(face: Typeface, size: Float, color: Int, spacing: Float) = TextView(context).apply {
        typeface = face; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color); letterSpacing = spacing
        includeFontPadding = false; gravity = Gravity.CENTER
    }

    private companion object {
        const val ROW = 33
        const val NAME_X = 52f
        const val NAME_MAX = 190f
        const val STATE_RIGHT = 10f
        const val GAP = 14f
        const val DISSOLVE = 220L
        const val FADE_BACK = 300L
    }
}

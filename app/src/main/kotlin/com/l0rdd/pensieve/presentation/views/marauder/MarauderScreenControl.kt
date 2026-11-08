package com.l0rdd.pensieve.presentation.views.marauder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.common.Saying
import com.l0rdd.pensieve.presentation.views.common.ScreenControl
import com.l0rdd.pensieve.presentation.views.common.movesScreenWhileHeld

/**
 * The Marauder's screen panel, laid in the house column where the legend was: a Gazette insert
 * ([WIDTH] wide, about [HEIGHT] high), "▤ ÉCRAN" and where the screen is, the screen drawn
 * unrolling beside three rows of ink buttons (auto ▲ ■ ▼, by hand ▲ ▼ while OK is held, "it is"
 * = rolled up = open), and what OK does on the focused one. It comes out of the parchment as the
 * Map does ([reveal]: an ink drop, tendrils, the panel blooming through them while the oath is
 * said) and goes back into its drop ([vanish], "mischief managed"). The arrows stay inside; BACK folds it.
 */
class MarauderScreenControl(
    private val context: Context,
    private val fonts: Fonts,
    clock: FrameClock,
    private val onCommand: (ScreenCommand) -> Unit,
    private val onClose: () -> Unit,
    private val saying: Saying,
) : ScreenControl {
    private val grid = ScreenGrid()
    private var at = grid.first
    private val state = text(fonts.fellItalic, 20f, InkColors.MUTED, 0f).apply { gravity = Gravity.END; isSingleLine = true }
    private val drawing = ScreenDrawing(context, fonts.fell, fonts.fellItalic, clock, context.getString(R.string.mm_screen_seance))
    private val hint = HintLine()
    private val buttons = ACTS.mapIndexed { r, row -> row.map { button(it, wide = r == 2) } }
    private val insert = Insert()
    private val parts: List<View>
    private var timeline: SpellTimeline? = null
    private var ink: SpellInk? = null
    private var ms = 0L
    private var animator: ValueAnimator? = null

    override val view: View get() = insert

    init {
        val header = header()
        val labels = WHAT.map { label(it) }
        val rows = buttons.map { row(it) }
        val controls = column().apply {
            labels.indices.forEach { i ->
                addView(labels[i], LinearLayout.LayoutParams(-1, -2).apply { if (i > 0) topMargin = 10 })
                addView(rows[i], LinearLayout.LayoutParams(-1, BUTTON).apply { topMargin = 2 })
            }
        }
        val body = LinearLayout(context).apply {
            clipChildren = false
            addView(drawing, LinearLayout.LayoutParams(ScreenDrawing.W, ScreenDrawing.H))
            addView(controls, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = 12 })
        }
        insert.addView(header, LinearLayout.LayoutParams(-1, -2))
        insert.addView(body, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 8 })
        insert.addView(hint, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 6 })
        parts = listOf(header, drawing) + labels.zip(rows).flatMap { (l, r) -> listOf(l, r) } + hint
    }

    override fun show(house: House) {
        state.text = gazetteWords(house)?.let { (words, numbers) -> context.getString(words, *numbers.toTypedArray()) } ?: ""
        drawing.show(house.screen)
    }

    override fun reveal() {
        focus(grid.first)
        saying.say(context.getString(R.string.mm_oath))
        cast(opening = true) {}
    }

    override fun vanish(then: () -> Unit) {
        saying.say(context.getString(R.string.mm_screen_mischief))
        cast(opening = false) { insert.visibility = View.INVISIBLE; then() }
    }

    /** Casts the spell, coming or going, on a [ValueAnimator] (the frame clock may be resting); [then] once it is over. */
    private fun cast(opening: Boolean, then: () -> Unit) {
        animator?.run { removeAllListeners(); cancel() }
        val spell = SpellTimeline(opening, parts.size)
        timeline = spell; ink = null
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = spell.length
            interpolator = LinearInterpolator()
            addUpdateListener { frame((it.animatedFraction * spell.length).toLong()) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { settle(opening); then() }
            })
        }
        frame(0)
        animator?.start()
    }

    /** The spell at [now]: each part as inked as it is, the ink and the mask redrawn. */
    private fun frame(now: Long) {
        val spell = timeline ?: return
        ms = now
        parts.forEachIndexed { i, part -> inkIn(part, spell.inked(i, now)) }
        insert.invalidate()
    }

    /** Come, the panel is all there, sharp, and the spell over; gone, it stays out of sight. */
    private fun settle(opening: Boolean) {
        if (!opening) return
        timeline = null; ink = null
        parts.forEach { inkIn(it, 1f) }
        insert.invalidate()
    }

    /** [part] from blurred away (0) to sharp (1). */
    private fun inkIn(part: View, p: Float) {
        part.alpha = p
        val blur = (1 - p) * 4
        part.setRenderEffect(if (p > 0f && p < 1f) RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.DECAL) else null)
    }

    /** What the panel does with [event]: true when it is done with it. */
    private fun key(event: KeyEvent): Boolean = when (panelKey(event.keyCode, event.action, spelling = timeline != null)) {
        PanelKey.CLOSE -> true.also { onClose() }
        PanelKey.SWALLOW -> true
        PanelKey.ARROW -> true.also { arrow(event) }
        PanelKey.PASS -> false
    }

    private fun arrow(event: KeyEvent) {
        val arrow = arrowOf(event.keyCode) ?: return
        if (event.action == KeyEvent.ACTION_DOWN) focus(grid.move(at, arrow))
    }

    /** Gives the focus to the button at [cell], and says what OK does on it. */
    private fun focus(cell: Cell) {
        at = cell
        buttons[cell.row][cell.column].requestFocus()
        hint.text = inFell(context.getText(ACTS[cell.row][cell.column].hint))
    }

    /** The bold words of a hint (OK, OK held) in the roman Fell, as the mockup's. */
    private fun inFell(words: CharSequence): CharSequence {
        val spans = SpannableString(words)
        spans.getSpans(0, spans.length, StyleSpan::class.java).forEach {
            spans.setSpan(TypefaceSpan(fonts.fell), spans.getSpanStart(it), spans.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spans.removeSpan(it)
        }
        return spans
    }

    private fun header(): View = object : LinearLayout(context) {
        private val rule = Paint().apply { color = InkColors.NEWS_INK; strokeWidth = 1f }
        init {
            setWillNotDraw(false); setPadding(0, 0, 0, 9)
            addView(text(fonts.fell, 24f, InkColors.RED, .14f).apply { text = "▤ " + context.getString(R.string.screen).uppercase() })
            addView(state, LayoutParams(0, -2, 1f).apply { leftMargin = 10 })
        }
        override fun onDraw(canvas: Canvas) {
            canvas.drawLine(0f, height - 2.5f, width.toFloat(), height - 2.5f, rule)
            canvas.drawLine(0f, height - .5f, width.toFloat(), height - .5f, rule)
        }
    }

    private fun label(words: Int) = text(fonts.fellItalic, 17f, InkColors.MUTED, 0f).apply { text = context.getText(words); isSingleLine = true }

    private fun row(row: List<InkButton>) = LinearLayout(context).apply {
        clipChildren = false
        row.forEachIndexed { i, b -> addView(b, LinearLayout.LayoutParams(0, -1, 1f).apply { if (i > 0) leftMargin = 8 }) }
    }

    private fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; clipChildren = false }

    /** An ink button for [act]: OK sends its command, or, by hand, holds it while OK is. */
    private fun button(act: Act, wide: Boolean) = InkButton(context, fonts.fell, act.label(context), InkColors.NEWS_INK, 1.5f).apply {
        setPadding(0, 0, 0, 0)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, if (wide) 17f else 20f)
        letterSpacing = if (wide) .04f else .08f
        if (act.held) movesScreenWhileHeld(act.command, onCommand) else setOnClickListener { onCommand(act.command) }
    }

    private fun text(face: Typeface, size: Float, color: Int, spacing: Float) = TextView(context).apply {
        typeface = face; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color); letterSpacing = spacing; includeFontPadding = false
    }

    /** What OK does on the focused button, under a thin rule. */
    private inner class HintLine : TextView(context) {
        private val rule = Paint().apply { color = InkColors.alpha(InkColors.NEWS_INK, .35f); strokeWidth = 1f }
        init {
            typeface = fonts.fellItalic; setTextSize(TypedValue.COMPLEX_UNIT_PX, 17f); setTextColor(InkColors.alpha(InkColors.NEWS_INK, .9f))
            includeFontPadding = false; setLineSpacing(0f, 1.18f); minLines = 2; maxLines = 2
            setPadding(0, 6, 0, 0)
        }
        override fun onDraw(canvas: Canvas) {
            canvas.drawLine(0f, .5f, width.toFloat(), .5f, rule)
            super.onDraw(canvas)
        }
    }

    /** The insert's cream paper, its red double frame and its shadow, slightly askew; seen through the spell while it runs. */
    private inner class Insert : LinearLayout(context) {
        private val paper = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CREAM; setShadowLayer(11f, 0f, 6f, SHADOW) }
        private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = InkColors.RED }

        init {
            orientation = VERTICAL; setPadding(16, 12, 16, 10)
            setWillNotDraw(false); clipChildren = false; rotation = -.35f
        }

        /** The drop, the shadow and the tilt go a little past the panel: the column must not clip them. */
        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            (parent as? ViewGroup)?.clipChildren = false
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat(); val h = height.toFloat()
            canvas.drawRect(0f, 0f, w, h, paper)
            canvas.drawRect(.5f, .5f, w - .5f, h - .5f, edge)
            canvas.drawRect(5.5f, 5.5f, w - 5.5f, h - 5.5f, edge)
        }

        override fun draw(canvas: Canvas) {
            val spell = spellInk() ?: return super.draw(canvas)
            spell.masked(canvas, ms) { plain(canvas) }
            spell.ink(canvas, ms)
        }

        private fun plain(canvas: Canvas) = super.draw(canvas)

        /** The spell's ink, made at the panel's size the first time it is drawn; null when no spell runs. */
        private fun spellInk(): SpellInk? {
            val spell = timeline ?: return null
            return ink ?: SpellInk(width.toFloat(), height.toFloat(), spell).also { ink = it }
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean = key(event) || super.dispatchKeyEvent(event)
    }

    /** A button of the panel: its label, what OK says on it, the command it sends, and whether it is held. */
    private class Act(private val text: Int, val glyph: String?, val hint: Int, val command: ScreenCommand, val held: Boolean = false) {
        fun label(context: Context): String = glyph ?: context.getString(text)
    }

    companion object {
        const val WIDTH = 410
        const val HEIGHT = 330
        private const val BUTTON = 40
        private const val CREAM = 0xFFF8E8C4.toInt()
        private const val SHADOW = 0x4D3C1905

        private val WHAT = listOf(R.string.mm_screen_auto, R.string.mm_screen_by_hand, R.string.mm_screen_is)
        private val ACTS = listOf(
            listOf(Act(0, "▲", R.string.screen_hint_raise, ScreenCommand.RAISE), Act(0, "■", R.string.screen_hint_stop, ScreenCommand.STOP), Act(0, "▼", R.string.screen_hint_lower, ScreenCommand.LOWER)),
            listOf(Act(0, "▲", R.string.screen_hint_up, ScreenCommand.PRESS_UP, held = true), Act(0, "▼", R.string.screen_hint_down, ScreenCommand.PRESS_DOWN, held = true)),
            listOf(Act(R.string.screen_is_rolled, null, R.string.screen_hint_rolled, ScreenCommand.ROLLED_UP), Act(R.string.screen_is_open, null, R.string.screen_hint_open, ScreenCommand.OPEN)),
        )
    }
}

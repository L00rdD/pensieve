package com.l0rdd.pensieve.presentation.views.marauder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.apps.ShelvedApp
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools
import com.l0rdd.pensieve.presentation.views.common.ActionTile
import com.l0rdd.pensieve.presentation.views.common.AppTile
import com.l0rdd.pensieve.presentation.views.common.ArtworkDrawing
import com.l0rdd.pensieve.presentation.views.common.ClockPanel
import com.l0rdd.pensieve.presentation.views.common.FrenchTime
import com.l0rdd.pensieve.presentation.views.common.HeroPanel
import com.l0rdd.pensieve.presentation.views.common.MenuStyle
import com.l0rdd.pensieve.presentation.views.common.ShelfSkin
import java.time.LocalDateTime

/**
 * The Marauder's Map: apps are rooms with their door, footsteps walk the corridors between them,
 * the resume is the Gazette's front page, the house its small ads, and the time Hermione's Time-Turner.
 */
class MarauderSkin(private val tools: HomeViewTools) : ShelfSkin {
    private val context = tools.context
    private val fonts = tools.fonts
    private val map = MapView(context, fonts.fellItalic, tools.clock)
    override val menuStyle = MenuStyle(fonts.fell, InkColors.INK, InkColors.RED, 0xF5F2DFB6.toInt(), 0x80461A05.toInt(), 32f, upper = false)

    override fun background(): View = map
    override fun overlay(): View = BurntEdges(context)
    override fun brand(): View = text(fonts.fellItalic, 36f, InkColors.INK, 0.03f).apply { text = context.getString(R.string.mm_title) }
    override fun line() = text(fonts.fellItalic, 27f, InkColors.MUTED, 0f).apply { text = context.getString(R.string.mm_oath); isSingleLine = true; ellipsize = TextUtils.TruncateAt.END }
    override fun time() = text(fonts.fell, 34f, InkColors.INK, 0.05f)
    override fun gear(): View = InkButton(context, fonts.fell, "⚙ " + context.getString(R.string.settings))
    override fun hint() = text(fonts.fellItalic, 23f, InkColors.INK, 0f).apply { setShadowLayer(8f, 0f, 0f, 0xE6FAE1AF.toInt()) }
    override fun onFocusMove(from: Rect?, to: Rect) { from?.let { map.walk(it, to) } }

    override fun label(title: String, hint: String?): View = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        addView(text(fonts.fell, 22f, InkColors.INK, 0f).apply { text = "❦" })
        addView(text(fonts.fell, 25f, InkColors.RED, 0.16f).apply { text = title.uppercase(); setPadding(14, 0, 14, 0) })
        hint?.let { addView(text(fonts.fellItalic, 24f, InkColors.MUTED, 0f).apply { text = it; setPadding(0, 0, 14, 0) }) }
        addView(object : View(context) {
            private val p = Paint().apply { color = InkColors.alpha(InkColors.INK, 0.55f); strokeWidth = 1.2f; pathEffect = DashPathEffect(floatArrayOf(7f, 6f), 0f) }
            override fun onDraw(canvas: Canvas) { canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, p) }
        }, LinearLayout.LayoutParams(0, 4, 1f))
    }

    override fun clock(): ClockPanel = object : ClockPanel {
        private val turner = TimeTurnerView(context, fonts.fell, context.getString(R.string.mm_inscription), tools.clock)
        private val words = text(fonts.fellItalic, 34f, InkColors.INK, 0f).apply { gravity = Gravity.CENTER }
        private val digits = text(fonts.fell, 16f, InkColors.RED, 0.2f).apply { gravity = Gravity.CENTER }
        private val date = text(fonts.fellItalic, 22f, InkColors.MUTED, 0f).apply { gravity = Gravity.CENTER }
        override val height = 452
        override val view: View = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            addView(turner, LinearLayout.LayoutParams(320, 336))
            addView(words, LinearLayout.LayoutParams(-1, -2))
            addView(digits, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 2 })
            addView(date, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 4 })
        }
        override fun show(now: LocalDateTime) {
            turner.show(now)
            words.text = FrenchTime.inWords(now.hour, now.minute)
            digits.text = FrenchTime.hm(now)
            date.text = FrenchTime.longDate(now)
        }
    }

    override fun hero(): HeroPanel = GazetteHero(context, fonts, tools.clock, context.getString(R.string.mm_gazette), context.getString(R.string.mm_special), context.getString(R.string.nothing_to_resume))

    override fun favoriteTile(): AppTile = Room(big = true)
    override fun appTile(): AppTile = Room(big = false)

    /** The house as a small ad of the Gazette: a double rule, the state stamped in red. */
    override fun action(): ActionTile = object : ActionTile {
        private var title = ""; private var detail = ""; private var state = ""; private var glyph = ""
        private val paper = Paint().apply { color = InkColors.NEWSPRINT }
        private val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = InkColors.NEWS_INK }
        private val name = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 23f; letterSpacing = 0.1f }
        private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 21f; color = InkColors.MUTED }
        private val stamp = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 16f; letterSpacing = 0.08f; color = InkColors.RED }
        override val view: View = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                val w = width.toFloat(); val h = height.toFloat()
                paper.color = if (isFocused) 0xFFF8E8C4.toInt() else InkColors.NEWSPRINT
                canvas.drawRect(0f, 0f, w, h, paper)
                val ink = if (isFocused) InkColors.RED else InkColors.NEWS_INK
                rule.color = ink; rule.strokeWidth = 1f; canvas.drawRect(0.5f, 0.5f, w - 0.5f, h - 0.5f, rule); canvas.drawRect(4.5f, 4.5f, w - 4.5f, h - 4.5f, rule)
                name.color = ink
                canvas.drawText(glyph, 34f, h / 2 + 9, name)
                canvas.drawText(title.uppercase(), 80f, 38f, name)
                canvas.drawText(detail, 80f, 66f, sub)
                if (state.isNotEmpty()) {
                    canvas.save(); canvas.rotate(-7f, w - 60f, h / 2)
                    val sw = stamp.measureText(state) + 16
                    rule.color = InkColors.RED; rule.strokeWidth = 2f
                    canvas.drawRect(w - 30f - sw, h / 2 - 15, w - 30f, h / 2 + 12, rule)
                    canvas.drawText(state, w - 22f - sw, h / 2 + 5, stamp)
                    canvas.restore()
                }
            }
        }
        override fun show(title: String, detail: String, state: String, glyph: String) { this.title = title; this.detail = detail; this.state = state; this.glyph = glyph; view.invalidate() }
    }

    /** A room of the castle: double walls with a door in the middle of the bottom wall. */
    private inner class Room(private val big: Boolean) : AppTile {
        private var app: ShelvedApp? = null; private var art: Bitmap? = null
        private val floor = Paint().apply { color = InkColors.ROOM }
        private val wall = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val name = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = if (big) 24f else 20f }
        private val sub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 20f; color = InkColors.MUTED }
        private val ribbon = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 19f; color = InkColors.INK; textAlign = Paint.Align.CENTER }
        private val tagBox = Paint()
        override val view: View = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                val a = app ?: return
                val w = width.toFloat(); val h = height.toFloat()
                canvas.drawRect(0f, 0f, w, h, floor.apply { color = if (isFocused) 0xF7FAE8C0.toInt() else InkColors.ROOM })
                val ink = if (isFocused) InkColors.RED else InkColors.INK
                val door = w * 0.24f; val dl = (w - door) / 2; val dr = dl + door
                wall.color = ink
                wall.strokeWidth = 2.4f; walls(canvas, 1.2f, w - 1.2f, h - 1.2f, dl, dr)
                wall.strokeWidth = 1.1f; walls(canvas, 6f, w - 6f, h - 6f, dl, dr)
                canvas.drawLine(dl, h - 6, dl, h, wall); canvas.drawLine(dr, h - 6, dr, h, wall)
                name.color = ink
                if (big) {
                    art?.let { ArtworkDrawing.fit(canvas, it, RectF(w - 150f, 16f, w - 18f, 88f)) }
                    canvas.drawText(TextUtils.ellipsize(a.app.label, name, w - 44f, TextUtils.TruncateAt.END).toString(), 22f, h - 52f, name)
                    canvas.drawText(if (a.isNew) "nouveau" else a.app.id.value.substringAfterLast('.'), 22f, h - 24f, sub)
                } else {
                    art?.let { ArtworkDrawing.fit(canvas, it, RectF(22f, 14f, w - 22f, h - 46f)) }
                    name.textAlign = Paint.Align.CENTER
                    canvas.drawText(TextUtils.ellipsize(a.app.label, name, w - 30f, TextUtils.TruncateAt.END).toString(), w / 2, h - 18f, name)
                    name.textAlign = Paint.Align.LEFT
                }
                if (a.isNew) { sub.color = InkColors.RED; canvas.drawText("nouveau", w - 82f, h - 14f, sub); sub.color = InkColors.MUTED }
                if (isFocused) {   // the name on its ribbon, as the map labels people
                    val tw = ribbon.measureText(a.app.label) + 24
                    tagBox.color = 0xFFF6E7C2.toInt(); canvas.drawRect(w / 2 - tw / 2, 0f, w / 2 + tw / 2, 26f, tagBox)
                    wall.strokeWidth = 1.5f; wall.color = InkColors.INK; canvas.drawRect(w / 2 - tw / 2, 0f, w / 2 + tw / 2, 26f, wall)
                    canvas.drawText(a.app.label, w / 2, 20f, ribbon)
                }
            }
            override fun onFocusChanged(g: Boolean, d: Int, r: android.graphics.Rect?) {
                super.onFocusChanged(g, d, r)
                animate().rotation(if (g) -0.6f else 0f).scaleX(if (g) 1.03f else 1f).scaleY(if (g) 1.03f else 1f).translationY(if (g) -3f else 0f).setDuration(180).start()
            }
        }

        private fun walls(canvas: Canvas, l: Float, r: Float, b: Float, dl: Float, dr: Float) {
            val path = android.graphics.Path().apply { moveTo(dl, b); lineTo(l, b); lineTo(l, l); lineTo(r, l); lineTo(r, b); lineTo(dr, b) }
            canvas.drawPath(path, wall)
        }

        override fun show(app: ShelvedApp, artwork: Bitmap?, index: Int) { this.app = app; this.art = artwork; view.invalidate() }
    }

    private fun text(face: Typeface, size: Float, color: Int, spacing: Float) = TextView(context).apply {
        typeface = face; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color); letterSpacing = spacing; includeFontPadding = false
    }
}

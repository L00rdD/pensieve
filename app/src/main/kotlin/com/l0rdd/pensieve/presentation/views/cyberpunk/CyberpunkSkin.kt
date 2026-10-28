package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.apps.AppKind
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

/** Cyberpunk: Netwatch's red and yellow over Night City; apps are executables, the resume a braindance. */
class CyberpunkSkin(private val tools: HomeViewTools) : ShelfSkin {
    private val context = tools.context
    private val fonts = tools.fonts
    override val menuStyle = MenuStyle(fonts.rajdhaniBold, CyberColors.TEXT, CyberColors.YELLOW, 0xF20B0507.toInt(), 0xB3000000.toInt(), 30f)

    override fun background(): View = NightCityView(context, fonts.rajdhaniBold, context.getString(R.string.cp_neon), tools.clock)
    override fun overlay(): View = CyberOverlay(context)
    override fun brand(): View = GlitchText(context, fonts.rajdhaniBold, 38f, CyberColors.YELLOW, tools.clock).apply { text = context.getString(R.string.cp_brand) }
    override fun line() = text(fonts.rajdhaniSemi, 20f, CyberColors.RED, 0.2f).apply { text = "NETWATCH // NODES ONLINE"; isSingleLine = true; ellipsize = TextUtils.TruncateAt.END }
    override fun time() = text(fonts.rajdhaniBold, 40f, CyberColors.TEXT, 0.04f)
    override fun gear(): View = CyberButton(context, fonts.rajdhaniBold, "⚙ " + context.getString(R.string.settings).uppercase())
    override fun hint() = text(fonts.rajdhaniSemi, 21f, CyberColors.MUTED, 0.06f)

    override fun label(title: String, hint: String?): View = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        addView(object : TextView(context) {
            private val bg = Paint().apply { color = CyberColors.YELLOW }
            init { text = title.uppercase(); typeface = fonts.rajdhaniBold; setTextSize(TypedValue.COMPLEX_UNIT_PX, 23f); letterSpacing = 0.14f; setTextColor(0xFF0B0B0B.toInt()); setPadding(14, 2, 24, 0) }
            override fun onDraw(canvas: Canvas) {
                canvas.drawPath(android.graphics.Path().apply { moveTo(0f, 0f); lineTo(width.toFloat(), 0f); lineTo(width - 10f, height.toFloat()); lineTo(0f, height.toFloat()); close() }, bg)
                super.onDraw(canvas)
            }
        })
        hint?.let { addView(text(fonts.rajdhaniSemi, 21f, CyberColors.RED, 0.08f).apply { text = it; setPadding(16, 0, 0, 0) }) }
        addView(object : View(context) {
            private val p = Paint()
            override fun onDraw(canvas: Canvas) { p.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), 0f, CyberColors.RED, 0, android.graphics.Shader.TileMode.CLAMP); canvas.drawRect(0f, height / 2f, width.toFloat(), height / 2f + 1, p) }
        }, LinearLayout.LayoutParams(0, 4, 1f).apply { leftMargin = 18 })
    }

    override fun clock(): ClockPanel = object : ClockPanel {
        private val digits = GlitchText(context, fonts.rajdhaniBold, 128f, CyberColors.TEXT, tools.clock)
        private val date = text(fonts.rajdhaniSemi, 20f, CyberColors.RED, 0.25f)
        override val height = 236
        override val view: View = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(label(context.getString(R.string.cp_clock), null).also { (it as LinearLayout).removeViewAt(it.childCount - 1) })
            addView(digits)
            addView(date)
        }
        override fun show(now: LocalDateTime) { digits.text = FrenchTime.hm(now); date.text = FrenchTime.shortDate(now) }
    }

    override fun hero(): HeroPanel = BraindanceHero(context, fonts, context.getString(R.string.resume), context.getString(R.string.restart), context.getString(R.string.nothing_to_resume))

    override fun favoriteTile(): AppTile = Tile(big = true)
    override fun appTile(): AppTile = Tile(big = false)

    override fun action(): ActionTile = object : ActionTile {
        private var title = ""; private var detail = ""; private var state = ""; private var glyph = ""
        private val name = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 21f; letterSpacing = 0.12f }
        private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhani; textSize = 19f; color = CyberColors.TEXT; alpha = 190 }
        private val end = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 17f; letterSpacing = 0.18f; color = CyberColors.CYAN; textAlign = Paint.Align.RIGHT }
        override val view: View = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                val w = width.toFloat(); val h = height.toFloat()
                CutFrame.draw(canvas, w, h, isFocused, 14f)
                name.color = if (isFocused) CyberColors.YELLOW else CyberColors.TEXT
                canvas.drawText(glyph, 36f, h / 2 + 8, name.apply { color = if (isFocused) CyberColors.YELLOW else CyberColors.RED })
                name.color = if (isFocused) CyberColors.YELLOW else CyberColors.TEXT
                canvas.drawText(title.uppercase(), 82f, 36f, name)
                canvas.drawText(detail, 82f, 64f, sub)
                canvas.drawText(state, w - 20f, h / 2 + 6, end)
            }
            override fun onFocusChanged(g: Boolean, d: Int, r: android.graphics.Rect?) { super.onFocusChanged(g, d, r); animate().translationX(if (g) 4f else 0f).setDuration(120).start() }
        }
        override fun show(title: String, detail: String, state: String, glyph: String) { this.title = title; this.detail = detail; this.state = state; this.glyph = glyph; view.invalidate() }
    }

    private inner class Tile(private val big: Boolean) : AppTile {
        private var app: ShelvedApp? = null; private var art: Bitmap? = null; private var index = 0
        private val name = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = if (big) 23f else 17f; letterSpacing = 0.06f }
        private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 15f; letterSpacing = 0.16f; color = CyberColors.MUTED }
        override val view: View = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                val a = app ?: return
                val w = width.toFloat(); val h = height.toFloat()
                CutFrame.draw(canvas, w, h, isFocused)
                val label = a.app.label.uppercase()
                if (big) {
                    canvas.drawText(if (isFocused) "ACCESS ▸" else "APP-%02d".format(index + 1), 20f, 28f, small.apply { color = if (isFocused) CyberColors.YELLOW else CyberColors.MUTED })
                    art?.let { ArtworkDrawing.fit(canvas, it, RectF(w - 150f, 16f, w - 16f, 86f)) }
                    drawName(canvas, label, 20f, h - 48f, w - 40f)
                    small.color = CyberColors.MUTED
                    canvas.drawText(if (a.isNew) "NOUVEAU" else a.app.id.value.substringAfterLast('.').uppercase(), 20f, h - 20f, small)
                } else {
                    art?.let { ArtworkDrawing.fit(canvas, it, RectF(20f, 14f, w - 20f, h - 44f)) }
                    if (a.app.kind == AppKind.STORE && art == null) canvas.drawText("+", w / 2 - 8, h / 2, name)
                    drawName(canvas, label, 14f, h - 16f, w - 28f)
                }
                if (a.isNew) { small.color = CyberColors.CYAN; canvas.drawText("NEW", w - 46f, 22f, small) }
            }
            override fun onFocusChanged(g: Boolean, d: Int, r: android.graphics.Rect?) {
                super.onFocusChanged(g, d, r)
                animate().scaleX(if (g) 1.04f else 1f).scaleY(if (g) 1.04f else 1f).translationY(if (g) -4f else 0f).setDuration(160).start()
                if (g) animate().translationX(-5f).setDuration(55).withEndAction { animate().translationX(4f).setDuration(55).withEndAction { animate().translationX(0f).setDuration(55).start() }.start() }.start()
            }
        }

        private fun drawName(canvas: Canvas, label: String, x: Float, y: Float, max: Float) {
            val focused = view.isFocused
            name.color = CyberColors.RED; canvas.drawText("//", x, y, name)
            val dx = name.measureText("//")
            val shown = TextUtils.ellipsize(label + if (big) ".EXE" else "", name, max - dx, TextUtils.TruncateAt.END).toString()
            if (focused) { name.color = CyberColors.alpha(CyberColors.RED, 0.8f); canvas.drawText(shown, x + dx + 2, y, name); name.color = CyberColors.alpha(CyberColors.CYAN, 0.7f); canvas.drawText(shown, x + dx - 2, y, name) }
            name.color = if (focused) CyberColors.YELLOW else CyberColors.TEXT
            canvas.drawText(shown, x + dx, y, name)
        }

        override fun show(app: ShelvedApp, artwork: Bitmap?, index: Int) { this.app = app; this.art = artwork; this.index = index; view.invalidate() }
    }

    private fun text(face: Typeface, size: Float, color: Int, spacing: Float) = TextView(context).apply {
        typeface = face; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color); letterSpacing = spacing; includeFontPadding = false
    }
}

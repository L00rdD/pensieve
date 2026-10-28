package com.l0rdd.pensieve.presentation.views.common

import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
import com.l0rdd.pensieve.domain.entities.apps.ShelvedApp
import com.l0rdd.pensieve.domain.entities.resume.Resume
import java.time.LocalDateTime

/**
 * How a look dresses the shelf layout (header, the house on the left, then what to resume,
 * the favourites and every app): it makes each part, the layout places them.
 */
interface ShelfSkin {
    val menuStyle: MenuStyle

    /** Behind everything; it may move, on the shared frame clock. */
    fun background(): View

    /** Over everything, letting the remote through (scanlines, burnt edges); null for none. */
    fun overlay(): View?

    fun brand(): View

    /** The line next to the brand (the AI, the feed, the oath). */
    fun line(): TextView

    fun time(): TextView

    fun gear(): View

    fun clock(): ClockPanel

    fun label(title: String, hint: String?): View

    fun hero(): HeroPanel

    fun favoriteTile(): AppTile

    fun appTile(): AppTile

    fun action(): ActionTile

    fun hint(): TextView

    /** The focus went from [from] to [to], both given in the layout's coordinates. */
    fun onFocusMove(from: android.graphics.Rect?, to: android.graphics.Rect) {}
}

/** The clock at the top of the house column. */
interface ClockPanel {
    val view: View
    val height: Int
    fun show(now: LocalDateTime)
}

/** What to resume, with its two buttons. */
interface HeroPanel {
    val view: View
    val resume: View
    val restart: View
    fun show(resume: Resume?, poster: Bitmap?, now: LocalDateTime?)
}

/** An app on the shelf. */
interface AppTile {
    val view: View
    fun show(app: ShelvedApp, artwork: Bitmap?, index: Int)
}

/** One thing of the house (the cinema mode, an outlet, the screen). */
interface ActionTile {
    val view: View
    fun show(title: String, detail: String, state: String, glyph: String)
}

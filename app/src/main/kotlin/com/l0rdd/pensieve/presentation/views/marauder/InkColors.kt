package com.l0rdd.pensieve.presentation.views.marauder

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.views.common.Palettes

/** The Map's warm parchment and its inks, from the house's looks.json; the Gazette's newsprint and the rooms' floor are its own. */
object InkColors {
    val INK: Int by lazy { Palettes[Look.MARAUDER].accent }
    val MUTED: Int by lazy { Palettes[Look.MARAUDER].muted }
    val RED: Int by lazy { Palettes[Look.MARAUDER].hot }
    val PAPER: Int by lazy { Palettes[Look.MARAUDER].bg }
    val NEWS_INK: Int by lazy { Palettes[Look.MARAUDER].text }
    const val NEWSPRINT = 0xFFF2DFB6.toInt()
    const val ROOM = 0xE0F6DEAC.toInt()

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}

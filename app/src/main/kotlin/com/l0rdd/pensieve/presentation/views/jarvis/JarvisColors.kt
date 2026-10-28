package com.l0rdd.pensieve.presentation.views.jarvis

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.views.common.Palettes

/** J.A.R.V.I.S.'s palette, from the house's looks.json. */
object JarvisColors {
    val BG: Int by lazy { Palettes[Look.JARVIS].bg }
    val CYAN: Int by lazy { Palettes[Look.JARVIS].accent }
    val GOLD: Int by lazy { Palettes[Look.JARVIS].accent2 }
    val TEXT: Int by lazy { Palettes[Look.JARVIS].text }
    val MUTED: Int by lazy { Palettes[Look.JARVIS].muted }

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}

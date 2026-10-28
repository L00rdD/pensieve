package com.l0rdd.pensieve.presentation.views.cyberpunk

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.views.common.Palettes

/** Netwatch's red and yellow, and Night City's cyan, from the house's looks.json. */
object CyberColors {
    val BG: Int by lazy { Palettes[Look.CYBERPUNK].bg }
    val RED: Int by lazy { Palettes[Look.CYBERPUNK].accent }
    val CYAN: Int by lazy { Palettes[Look.CYBERPUNK].accent2 }
    val YELLOW: Int by lazy { Palettes[Look.CYBERPUNK].hot }
    val TEXT: Int by lazy { Palettes[Look.CYBERPUNK].text }
    val MUTED: Int by lazy { Palettes[Look.CYBERPUNK].muted }

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}

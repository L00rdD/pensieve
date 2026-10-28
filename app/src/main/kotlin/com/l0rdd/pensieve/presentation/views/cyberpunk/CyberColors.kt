package com.l0rdd.pensieve.presentation.views.cyberpunk

/** Netwatch's red and yellow, and Night City's cyan. */
object CyberColors {
    const val BG = 0xFF07080D.toInt()
    const val RED = 0xFFFF6158.toInt()
    const val CYAN = 0xFF5EF6FF.toInt()
    const val YELLOW = 0xFFF3E600.toInt()
    const val TEXT = 0xFFF4ECE7.toInt()
    const val MUTED = 0xFFA2726D.toInt()

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}

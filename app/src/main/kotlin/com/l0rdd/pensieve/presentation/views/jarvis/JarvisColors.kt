package com.l0rdd.pensieve.presentation.views.jarvis

/** J.A.R.V.I.S.'s palette, after the hub's Reactor page. */
object JarvisColors {
    const val BG = 0xFF02070D.toInt()
    const val CYAN = 0xFF6FE3FF.toInt()
    const val GOLD = 0xFFFFC857.toInt()
    const val TEXT = 0xFFE4F8FF.toInt()
    const val MUTED = 0xFF7FA9B8.toInt()

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}

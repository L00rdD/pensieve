package com.l0rdd.pensieve.domain.entities.house

/** The projection screen: [position] 0 when rolled up, 100 when all the way down. */
data class Screen(
    val id: String,
    val name: String,
    val position: Int,
    val moving: ScreenMove?,
) {
    val isDown: Boolean
        get() = position >= DOWN

    companion object {
        const val UP = 0
        const val DOWN = 100
    }
}

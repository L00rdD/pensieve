package com.l0rdd.pensieve.domain.entities.house

/** What the hub knows of the house; [reachable] is false when the hub does not answer. */
data class House(
    val outlets: List<Outlet>,
    val screen: Screen?,
    val reachable: Boolean,
) {
    /** The cinema mode is on when its outlets are off and the screen is down. */
    val isCinema: Boolean
        get() = outlets.filter { it.cinema }.none { it.on } && screen?.isDown == true

    companion object {
        val UNKNOWN = House(emptyList(), null, reachable = false)
    }
}

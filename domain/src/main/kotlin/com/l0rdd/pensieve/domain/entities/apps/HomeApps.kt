package com.l0rdd.pensieve.domain.entities.apps

/** The apps on the home screen: the favourites in the user's order, then every other app. */
data class HomeApps(
    val favorites: List<ShelvedApp>,
    val others: List<ShelvedApp>,
) {
    companion object {
        val EMPTY = HomeApps(emptyList(), emptyList())
    }
}

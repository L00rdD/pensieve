package com.l0rdd.pensieve.domain.entities.apps

/**
 * How the user arranged the apps: the favourites in their order, the hidden ones,
 * and the ones already seen (an app never seen is new).
 */
data class Shelf(
    val favorites: List<AppId>,
    val hidden: Set<AppId>,
    val seen: Set<AppId>,
)

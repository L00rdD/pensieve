package com.l0rdd.pensieve.domain.entities.house

/** An outlet of the hub (a lamp, a neon sign); [cinema] when the cinema mode switches it off. */
data class Outlet(
    val id: OutletId,
    val name: String,
    val on: Boolean,
    val cinema: Boolean,
)

package com.l0rdd.pensieve.domain.entities.house

/**
 * Someone at a place of the house (the desk), as the hub knows from its keyboard and mouse: its
 * [outlet] goes on when someone sits there and off once nobody has for a while; [idleSeconds]
 * since the last key or mouse move, null when the hub has never heard of it.
 */
data class Presence(
    val id: String,
    val name: String,
    val outlet: OutletId?,
    val present: Boolean,
    val idleSeconds: Long?,
)

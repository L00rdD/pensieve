package com.l0rdd.pensieve.domain.entities.scenes

/** A room the projector remembers (its keystone correction, zoom and position), as saved in JMGO's settings. */
data class Scene(val id: Int, val name: String, val current: Boolean)

package com.l0rdd.pensieve.domain.entities.sources

/** A picture source plugged into the projector (an HDMI port), [lit] when what is plugged in sends a picture. */
data class Source(val id: String, val name: String, val lit: Boolean)

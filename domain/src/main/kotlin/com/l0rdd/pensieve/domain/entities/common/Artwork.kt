package com.l0rdd.pensieve.domain.entities.common

/** A picture as encoded bytes (an app's banner or icon, a poster), identified by [key]. */
class Artwork(val key: String, val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean = other is Artwork && other.key == key
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "Artwork($key, ${bytes.size} bytes)"
}

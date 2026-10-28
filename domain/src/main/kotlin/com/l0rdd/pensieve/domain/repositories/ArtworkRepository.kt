package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.common.Artwork

/** Pictures from the network (posters, backdrops). */
interface ArtworkRepository {
    /** The picture at [url], or null when it cannot be had. */
    suspend fun load(url: String): Artwork?
}

package com.l0rdd.pensieve.data.repositories.resume

import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.domain.entities.common.Artwork
import com.l0rdd.pensieve.domain.repositories.ArtworkRepository

/**
 * Posters and backdrops over HTTP. Paths starting with /Items/ are Jellyfin's, asked of
 * [jellyfinUrl] at the size the home screen needs. The last [keep] pictures stay in memory.
 */
class HttpArtworkRepository(
    private val http: HttpDataSource,
    private val jellyfinUrl: String,
    private val keep: Int = 8,
) : ArtworkRepository {
    private val cache = object : LinkedHashMap<String, Artwork>(keep, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Artwork>?) = size > keep
    }

    override suspend fun load(url: String): Artwork? {
        synchronized(cache) { cache[url] }?.let { return it }
        val full = if (url.startsWith("/Items/")) "$jellyfinUrl$url?maxHeight=$MAX_HEIGHT&quality=85" else url
        val artwork = runCatching { Artwork(url, http.getBytes(full)) }.getOrNull() ?: return null
        synchronized(cache) { cache[url] = artwork }
        return artwork
    }

    private companion object {
        const val MAX_HEIGHT = 720
    }
}

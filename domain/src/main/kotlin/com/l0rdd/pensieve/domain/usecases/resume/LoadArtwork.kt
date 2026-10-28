package com.l0rdd.pensieve.domain.usecases.resume

import com.l0rdd.pensieve.domain.entities.common.Artwork
import com.l0rdd.pensieve.domain.repositories.ArtworkRepository
import com.l0rdd.pensieve.domain.usecases.UseCase

/** Fetches a poster or a backdrop by its URL. */
class LoadArtwork(private val artwork: ArtworkRepository) : UseCase<Artwork?, String> {

    override suspend fun invoke(param: String): Artwork? = artwork.load(param)
}

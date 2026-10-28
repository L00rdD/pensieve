package com.l0rdd.pensieve.data.repositories.resume

import android.content.Intent
import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.data.datasources.FeedDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import com.l0rdd.pensieve.data.models.FeedResumeModel
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

/**
 * What to resume, from the "resume" list of errol's feed, as the house pushes it. A document
 * that cannot be read changes nothing. Playing opens the item in the Jellyfin app.
 */
class FeedResumeRepository(
    private val feed: FeedDataSource,
    private val apps: AppsDataSource,
    private val mapper: FeedResumeMapper,
) : ResumeRepository {

    override fun observeResume(): Flow<Resume?> = feed.observe().transform { text ->
        runCatching { FeedResumeModel.parseList(text).firstOrNull()?.let(mapper::toResume) }.onSuccess { emit(it) }
    }

    /**
     * Opens the item's page in Jellyfin, which offers to resume or to start over; [fromStart]
     * cannot be passed to the app, so both open the same page.
     */
    override suspend fun play(resume: Resume, fromStart: Boolean) = withContext(Dispatchers.Main) {
        val item = Intent().setClassName(JELLYFIN, "$JELLYFIN.ui.startup.StartupActivity").putExtra("ItemId", resume.itemId)
        if (!apps.start(item)) apps.launch(JELLYFIN)
    }

    private companion object {
        const val JELLYFIN = "org.jellyfin.androidtv"
    }
}

package com.l0rdd.pensieve.data.repositories.resume

import android.content.Intent
import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import com.l0rdd.pensieve.data.models.FeedResumeModel
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * What to resume, from the "resume" list of errol's feed ([feedUrl]), read every [pollMs].
 * A feed that does not answer keeps the last answer. Playing opens the item in the Jellyfin app.
 */
class FeedResumeRepository(
    private val http: HttpDataSource,
    private val apps: AppsDataSource,
    private val mapper: FeedResumeMapper,
    private val feedUrl: String,
    private val pollMs: Long = 15_000,
) : ResumeRepository {

    override fun observeResume(): Flow<Resume?> = flow {
        var last: Resume? = null
        while (true) {
            last = runCatching { FeedResumeModel.parseList(http.getText(feedUrl)).firstOrNull()?.let(mapper::toResume) }.getOrDefault(last)
            emit(last)
            delay(pollMs)
        }
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

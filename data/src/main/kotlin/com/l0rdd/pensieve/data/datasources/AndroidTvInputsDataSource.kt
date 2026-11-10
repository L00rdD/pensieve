package com.l0rdd.pensieve.data.datasources

import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Handler
import android.os.Looper
import com.l0rdd.pensieve.data.models.TvInputModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * [TvInputsDataSource] on the TvInputManager. An input plays in the passthrough player that Google TV's
 * own input panel opens (com.google.android.tv.inputplayer), which answers a typed channel intent only.
 */
class AndroidTvInputsDataSource(private val context: Context, private val apps: AppsDataSource) : TvInputsDataSource {
    private val manager: TvInputManager? get() = context.getSystemService(TvInputManager::class.java)

    override fun query(): List<TvInputModel> {
        val manager = manager ?: return emptyList()
        return manager.tvInputList.map { info ->
            val label = info.loadCustomLabel(context)?.takeIf { it.isNotBlank() } ?: info.loadLabel(context)
            TvInputModel(info.id, label.toString(), info.type == TvInputInfo.TYPE_HDMI, info.isHidden(context), manager.getInputState(info.id))
        }
    }

    override fun changes(): Flow<Unit> = callbackFlow {
        val callback = object : TvInputManager.TvInputCallback() {
            override fun onInputStateChanged(inputId: String, state: Int) { trySend(Unit) }
            override fun onInputAdded(inputId: String) { trySend(Unit) }
            override fun onInputRemoved(inputId: String) { trySend(Unit) }
            override fun onInputUpdated(inputId: String) { trySend(Unit) }
        }
        val manager = manager
        manager?.registerCallback(callback, Handler(Looper.getMainLooper()))
        trySend(Unit)
        awaitClose { manager?.unregisterCallback(callback) }
    }

    override fun play(id: String): Boolean = apps.start(
        Intent(Intent.ACTION_VIEW).setDataAndType(TvContract.buildChannelUriForPassthroughInput(id), TvContract.Channels.CONTENT_ITEM_TYPE),
    )
}

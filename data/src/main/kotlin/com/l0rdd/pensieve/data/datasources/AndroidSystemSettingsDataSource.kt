package com.l0rdd.pensieve.data.datasources

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** [SystemSettingsDataSource] on Settings.System, followed with a content observer. */
class AndroidSystemSettingsDataSource(private val context: Context) : SystemSettingsDataSource {

    override fun read(key: String): String? = Settings.System.getString(context.contentResolver, key)

    override fun changes(vararg keys: String): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { trySend(Unit) }
        }
        keys.forEach { context.contentResolver.registerContentObserver(Settings.System.getUriFor(it), false, observer) }
        trySend(Unit)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }
}

package com.l0rdd.pensieve.data.datasources

import kotlinx.coroutines.flow.Flow

/** Android's system settings table, read by key and followed as it changes. */
interface SystemSettingsDataSource {
    fun read(key: String): String?

    /** Fires once now, then whenever one of [keys] changes. */
    fun changes(vararg keys: String): Flow<Unit>
}

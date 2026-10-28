package com.l0rdd.pensieve.data.datasources

/** The home screen's own settings, kept on the projector, as text by key. */
interface PreferencesDataSource {
    fun read(key: String): String?

    fun write(key: String, value: String)
}

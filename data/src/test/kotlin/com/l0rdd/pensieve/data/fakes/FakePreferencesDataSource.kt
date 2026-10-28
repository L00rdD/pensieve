package com.l0rdd.pensieve.data.fakes

import com.l0rdd.pensieve.data.datasources.PreferencesDataSource

/** Preferences in a map. */
class FakePreferencesDataSource(val values: MutableMap<String, String> = mutableMapOf()) : PreferencesDataSource {
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String) { values[key] = value }
}

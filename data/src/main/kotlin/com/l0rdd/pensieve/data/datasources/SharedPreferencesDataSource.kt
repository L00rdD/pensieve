package com.l0rdd.pensieve.data.datasources

import android.content.Context
import android.content.SharedPreferences

/** [PreferencesDataSource] in the app's shared preferences. */
class SharedPreferencesDataSource(context: Context) : PreferencesDataSource {
    private val prefs: SharedPreferences = context.getSharedPreferences("pensieve", Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

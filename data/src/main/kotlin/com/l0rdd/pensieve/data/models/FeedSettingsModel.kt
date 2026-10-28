package com.l0rdd.pensieve.data.models

import org.json.JSONObject

/** The "settings" of errol's feed (screen.json): the look every screen of the house shows, or null for each one's own. */
data class FeedSettingsModel(val look: String?, val followHub: Boolean) {
    companion object {
        fun parse(text: String): FeedSettingsModel {
            val settings = JSONObject(text).optJSONObject("settings") ?: return FeedSettingsModel(null, false)
            val look = if (settings.isNull("look")) null else settings.optString("look").ifEmpty { null }
            return FeedSettingsModel(look, settings.optBoolean("follow_hub"))
        }
    }
}

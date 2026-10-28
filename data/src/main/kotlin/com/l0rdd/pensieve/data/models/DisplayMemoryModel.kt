package com.l0rdd.pensieve.data.models

import org.json.JSONArray

/** One entry of JMGO's jmgo_display_memory setting (a saved scene), the parts the home screen uses. */
data class DisplayMemoryModel(val id: Int, val sceneName: String, val recentUsageTime: Long) {
    companion object {
        fun parseList(text: String?): List<DisplayMemoryModel> {
            if (text.isNullOrBlank()) return emptyList()
            val list = runCatching { JSONArray(text) }.getOrNull() ?: return emptyList()
            return (0 until list.length()).map { list.getJSONObject(it) }
                .map { DisplayMemoryModel(it.getInt("id"), it.optString("sceneName").trim(), it.optLong("recentUsageTime")) }
        }
    }
}

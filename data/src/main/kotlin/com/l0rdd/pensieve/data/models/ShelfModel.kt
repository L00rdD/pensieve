package com.l0rdd.pensieve.data.models

import org.json.JSONArray
import org.json.JSONObject

/** The shelf as stored in the preferences, by package names. */
data class ShelfModel(val favorites: List<String>, val hidden: Set<String>, val seen: Set<String>) {

    fun toJson(): String = JSONObject()
        .put("favorites", JSONArray(favorites))
        .put("hidden", JSONArray(hidden.toList()))
        .put("seen", JSONArray(seen.toList()))
        .toString()

    companion object {
        fun parse(text: String): ShelfModel {
            val json = JSONObject(text)
            fun list(key: String): List<String> = json.optJSONArray(key)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
            return ShelfModel(list("favorites"), list("hidden").toSet(), list("seen").toSet())
        }
    }
}

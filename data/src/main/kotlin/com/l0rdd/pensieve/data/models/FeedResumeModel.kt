package com.l0rdd.pensieve.data.models

import org.json.JSONObject

/** One entry of the "resume" list in errol's feed (screen.json); times in seconds. */
data class FeedResumeModel(
    val id: String,
    val title: String,
    val series: String?,
    val season: Int?,
    val episode: Int?,
    val year: Int?,
    val runtime: Long,
    val position: Long,
    val genres: List<String>,
    val overview: String,
    val poster: String?,
    val backdrop: String?,
) {
    companion object {
        /** The feed's resume list, the most recent first; empty when the feed has none. */
        fun parseList(text: String): List<FeedResumeModel> {
            val list = JSONObject(text).optJSONArray("resume") ?: return emptyList()
            return (0 until list.length()).map { list.getJSONObject(it) }.map { o ->
                val genres = o.optJSONArray("genres")
                FeedResumeModel(
                    id = o.getString("id"),
                    title = o.optString("title"),
                    series = o.optNullString("series"),
                    season = o.optNullInt("season"),
                    episode = o.optNullInt("episode"),
                    year = o.optNullInt("year"),
                    runtime = o.optLong("runtime"),
                    position = o.optLong("position"),
                    genres = (0 until (genres?.length() ?: 0)).map { genres!!.getString(it) },
                    overview = o.optString("overview"),
                    poster = o.optNullString("poster"),
                    backdrop = o.optNullString("backdrop"),
                )
            }
        }

        private fun JSONObject.optNullString(key: String): String? = if (isNull(key)) null else optString(key).ifEmpty { null }
        private fun JSONObject.optNullInt(key: String): Int? = if (isNull(key) || !has(key)) null else optInt(key)
    }
}

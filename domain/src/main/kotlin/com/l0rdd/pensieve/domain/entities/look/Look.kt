package com.l0rdd.pensieve.domain.entities.look

/** The home screen's three worlds, with their id in the house's looks.json (the name the feed uses). */
enum class Look(val id: String) {
    JARVIS("friday"),
    CYBERPUNK("cyberpunk"),
    MARAUDER("hogwarts");

    companion object {
        fun fromId(id: String?): Look? = entries.firstOrNull { it.id == id }
    }
}

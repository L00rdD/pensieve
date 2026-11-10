package com.l0rdd.pensieve.data.models

/** One of Android's TV inputs: its id, its label (the user's own first), and what Android says of it. */
data class TvInputModel(val id: String, val label: String, val hdmi: Boolean, val hidden: Boolean, val state: Int) {
    companion object {
        /** TvInputManager.INPUT_STATE_CONNECTED: something is plugged in and sends a picture. */
        const val CONNECTED = 0
    }
}

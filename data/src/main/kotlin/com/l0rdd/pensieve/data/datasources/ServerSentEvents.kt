package com.l0rdd.pensieve.data.datasources

import java.io.BufferedReader

/** Reads Server-Sent Events: each event's data (its `data:` lines joined); comments and other fields are skipped. */
object ServerSentEvents {
    fun read(reader: BufferedReader): Sequence<String> = sequence {
        val data = StringBuilder()
        while (true) {
            val line = reader.readLine() ?: break
            when {
                line.isEmpty() -> if (data.isNotEmpty()) { yield(data.toString()); data.clear() }
                line.startsWith("data:") -> { if (data.isNotEmpty()) data.append('\n'); data.append(line.removePrefix("data:").removePrefix(" ")) }
            }
        }
        if (data.isNotEmpty()) yield(data.toString())
    }
}

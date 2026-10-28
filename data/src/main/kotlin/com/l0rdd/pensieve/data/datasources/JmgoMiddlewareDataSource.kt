package com.l0rdd.pensieve.data.datasources

/** JMGO's middleware (in com.jmgo.hippo): integer settings of the light engine, by key. */
interface JmgoMiddlewareDataSource {
    /** Sets [key] to [value]; returns the middleware's answer. Fails when the middleware does not answer. */
    suspend fun set(key: Int, value: Int): Int
}

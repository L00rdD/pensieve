package com.l0rdd.pensieve.data.datasources

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * JMGO's middleware over its binder, without its library: binds com.jmgo.hippo's LocalService
 * (IJmGOSystemService), asks it for the DLP manager (IJmGODlpManagerService) and calls set(key, value).
 * The interfaces' descriptors and transaction codes come from the firmware's hippo.
 */
class BinderJmgoMiddlewareDataSource(private val context: Context) : JmgoMiddlewareDataSource {
    private val mutex = Mutex()
    private var dlp: IBinder? = null

    override suspend fun set(key: Int, value: Int): Int = withContext(Dispatchers.IO) {
        val manager = mutex.withLock { dlp?.takeIf { it.isBinderAlive } ?: connect().also { dlp = it } }
        transact(manager, DLP, SET) { writeInt(key); writeInt(value) }.readInt()
    }

    private suspend fun connect(): IBinder {
        val system = withTimeout(5_000) {
            suspendCancellableCoroutine { cont ->
                val connection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder) { if (cont.isActive) cont.resume(service) }
                    override fun onServiceDisconnected(name: ComponentName?) { dlp = null }
                }
                val intent = Intent(ACTION).setClassName(HIPPO, "com.jmgo.middleware.service.LocalService")
                check(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) { "JMGO middleware not found" }
            }
        }
        return checkNotNull(transact(system, SYSTEM, GET_DLP_MANAGER) {}.readStrongBinder()) { "no DLP manager" }
    }

    private fun transact(binder: IBinder, descriptor: String, code: Int, args: Parcel.() -> Unit): Parcel {
        val data = Parcel.obtain(); val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(descriptor); data.args()
            binder.transact(code, data, reply, 0)
            reply.readException()
            return reply
        } finally { data.recycle() }
    }

    private companion object {
        const val HIPPO = "com.jmgo.hippo"
        const val ACTION = "action.jmgo.middleware.service"
        const val SYSTEM = "com.jmgo.middleware.service.IJmGOSystemService"
        const val DLP = "com.jmgo.middleware.dlp.IJmGODlpManagerService"
        const val GET_DLP_MANAGER = 5
        const val SET = 1
    }
}

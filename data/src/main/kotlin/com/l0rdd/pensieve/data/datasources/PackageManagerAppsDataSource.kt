package com.l0rdd.pensieve.data.datasources

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** [AppsDataSource] on the package manager. */
class PackageManagerAppsDataSource(private val context: Context) : AppsDataSource {
    private val pm: PackageManager = context.packageManager

    override fun query(): List<InstalledApp> {
        val tv = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0)
        val phone = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        return (tv + phone)
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString(), encode(artwork(it))) }
    }

    override fun packages(): List<String> {
        val tv = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0)
        val phone = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        return (tv + phone).map { it.activityInfo.packageName }.distinct().filter { it != context.packageName }
    }

    override fun changes(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) { trySend(Unit) }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    override fun launch(packageName: String) {
        val intent = pm.getLeanbackLaunchIntentForPackage(packageName) ?: pm.getLaunchIntentForPackage(packageName) ?: return
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun start(intent: Intent): Boolean = runCatching {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
    }.getOrDefault(false)

    private fun artwork(info: ResolveInfo): Drawable? =
        info.activityInfo.loadBanner(pm) ?: info.activityInfo.applicationInfo.loadBanner(pm) ?: info.loadIcon(pm)

    /** The artwork as PNG, no larger than a TV banner (320 × 180) needs. */
    private fun encode(drawable: Drawable?): ByteArray? {
        drawable ?: return null
        val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: MAX_SIDE
        val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: MAX_SIDE
        val scale = minOf(1f, MAX_WIDTH.toFloat() / w, MAX_SIDE.toFloat() / h)
        val bitmap = (drawable as? BitmapDrawable)?.bitmap?.takeIf { scale == 1f }
            ?: Bitmap.createBitmap((w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888).also {
                val canvas = Canvas(it)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        return ByteArrayOutputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); out.toByteArray() }
    }

    private companion object {
        const val MAX_WIDTH = 320
        const val MAX_SIDE = 192
    }
}

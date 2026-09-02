package org.awaremate.shared

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

object AppContextProvider {
    var appContext: Context? = null
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun hasUsageStatsPermission(context: Any?): Boolean {
    val ctx = (context as? Context) ?: AppContextProvider.appContext ?: return false
    val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

actual fun openUsageAccessSettings(context: Any?) {
    val ctx = (context as? Context) ?: AppContextProvider.appContext ?: return
    try {
        val packageIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", ctx.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(packageIntent)
    } catch (_: Exception) {
        val generalIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(generalIntent)
    }
}

actual fun openBrowserUrl(url: String, context: Any?) {
    val ctx = (context as? Context) ?: AppContextProvider.appContext ?: return
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(browserIntent)
    } catch (_: Exception) {
        // Gracefully handle cases where no browser is available
    }
}

package app.banafsh.android.util

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

@JvmInline
value class ToastDuration private constructor(internal val length: Int) {
    companion object {
        val Short = ToastDuration(length = Toast.LENGTH_SHORT)
        val Long = ToastDuration(length = Toast.LENGTH_LONG)
    }
}

fun Context.toast(message: String, duration: ToastDuration = ToastDuration.Short) =
    Toast.makeText(this, message, duration.length).show()

fun Context.hasPermissions(permissions: Array<String>) = permissions.all { permission -> hasPermission(permission) }

fun Context.hasPermission(permission: String) = ContextCompat.checkSelfPermission(
    applicationContext,
    permission,
) == PackageManager.PERMISSION_GRANTED

val Context.isIgnoringBatteryOptimizations
    get() = !isAtLeastAndroid6 ||
        getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) ?: true

inline fun <reified T> Context.intent(): Intent = Intent(this, T::class.java)

inline fun <reified T : BroadcastReceiver> Context.broadcastPendingIntent(
    requestCode: Int = 0,
    flags: Int = if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0,
): PendingIntent = PendingIntent.getBroadcast(this, requestCode, intent<T>(), flags)

inline fun <reified T : Activity> Context.activityPendingIntent(
    requestCode: Int = 0,
    @PendingIntentCompat.Flags flags: Int = 0,
    block: Intent.() -> Unit = { },
) = pendingIntent(
    intent = intent<T>().apply(block),
    requestCode = requestCode,
    flags = flags,
)

fun Context.pendingIntent(
    intent: Intent,
    requestCode: Int = 0,
    @PendingIntentCompat.Flags flags: Int = 0,
): PendingIntent = PendingIntent.getActivity(
    /* context = */ this,
    /* requestCode = */ requestCode,
    /* intent = */ intent,
    /* flags = */ (if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0) or flags,
)

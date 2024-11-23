package app.banafsh.android.util

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat

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

package app.banafsh.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import app.banafsh.android.preference.AppearancePreferences
import app.banafsh.android.util.isAtLeastAndroid6
import app.banafsh.android.util.isAtLeastAndroid8
import com.google.material_color_utilities.hct.Hct
import com.google.material_color_utilities.scheme.SchemeTonalSpot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BanafshTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) = with(AppearancePreferences) {
    val isDark = colorMode == ColorMode.Dark || (colorMode == ColorMode.System && isSystemInDarkTheme)

    val context = LocalContext.current
    (context as Activity).SystemBarAppearance(isDark)

    MaterialTheme(
        colorScheme =
            SchemeTonalSpot(
                Hct.fromInt(0xffff0000.toInt()),
                isDark,
                0.0,
            ).toColorScheme(),
        typography = Typography,
        content = content,
    )
}

fun Activity.setSystemBarAppearance(isDark: Boolean) {
    with(WindowCompat.getInsetsController(window, window.decorView.rootView)) {
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
    }

    val color = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()

    if (!isAtLeastAndroid6) window.statusBarColor = color
    if (!isAtLeastAndroid8) window.navigationBarColor = color
}

@Composable
fun Activity.SystemBarAppearance(isDark: Boolean) =
    LaunchedEffect(isDark) {
        withContext(Dispatchers.Main) {
            setSystemBarAppearance(isDark)
        }
    }

package app.banafsh.android.ui.theme

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import app.banafsh.android.preference.AppearancePreferences
import com.google.material_color_utilities.hct.Hct
import com.google.material_color_utilities.scheme.SchemeTonalSpot

@Composable
fun AppTheme(
    sampleBitmap: Bitmap?,
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = with(AppearancePreferences) {
    val isDark = colorMode == ColorMode.Dark || (colorMode == ColorMode.System && isSystemInDarkTheme)

    val context = LocalContext.current as ComponentActivity

    val colorScheme = remember(sampleBitmap, colorSource, isDark) {
        when {
            colorSource == ColorSource.Default -> SchemeTonalSpot(
                Hct.fromInt(baseColor.toArgb()),
                isDark,
                0.0,
            ).toColorScheme()

            sampleBitmap == null -> SchemeTonalSpot(
                Hct.fromInt(baseColor.toArgb()),
                isDark,
                0.0,
            ).toColorScheme()

            else -> {
                val paletteColor = Palette
                    .from(sampleBitmap)
                    .maximumColorCount(8)
                    .generate()
                    .getDominantColor(baseColor.toArgb())

                SchemeTonalSpot(
                    Hct.fromInt(paletteColor),
                    isDark,
                    0.0,
                ).toColorScheme()
            }
        }
    }

    DisposableEffect(isDark) {
        context.enableEdgeToEdge(
            statusBarStyle = if (!isDark) {
                SystemBarStyle.light(
                    Color.Black.copy(alpha = 0.2f).toArgb(),
                    Color.Transparent.toArgb(),
                )
            } else {
                SystemBarStyle.dark(
                    Color.Transparent.toArgb(),
                )
            },
            navigationBarStyle = if (!isDark) {
                SystemBarStyle.light(
                    Color.Black.copy(alpha = 0.2f).toArgb(),
                    Color.Transparent.toArgb(),
                )
            } else {
                SystemBarStyle.dark(Color.Transparent.toArgb())
            },
        )

        onDispose { }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

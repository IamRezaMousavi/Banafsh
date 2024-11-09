package app.banafsh.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.material_color_utilities.hct.Hct
import com.google.material_color_utilities.scheme.SchemeTonalSpot

@Composable
fun BanafshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme =
            SchemeTonalSpot(
                Hct.fromInt(0xffff0000.toInt()),
                darkTheme,
                0.0,
            ).toColorScheme(),
        typography = Typography,
        content = content,
    )
}

package app.banafsh.android.ui.screen.player

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import app.banafsh.android.R
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun AnimatedPlayPauseButton(playing: Boolean, modifier: Modifier = Modifier) {
    val themeColor = MaterialTheme.colorScheme.onPrimaryContainer
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.play_pause))

    val colorFilter = remember(themeColor) {
        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            /* color = */ themeColor.toArgb(),
            /* blendModeCompat = */ BlendModeCompat.SRC_ATOP,
        )
    }
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = colorFilter,
            keyPath = arrayOf("**"),
        ),
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        speed = if (playing) 2f else -2f,
    )

    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties,
    )
}

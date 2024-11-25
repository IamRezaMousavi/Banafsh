package app.banafsh.android.preference

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.banafsh.android.GlobalPreferencesHolder
import app.banafsh.android.R

object PlayerPreferences : GlobalPreferencesHolder() {
    val isInvincibilityEnabledProperty = boolean(false)
    var isInvincibilityEnabled by isInvincibilityEnabledProperty
    val trackLoopEnabledProperty = boolean(false)
    var trackLoopEnabled by trackLoopEnabledProperty
    val queueLoopEnabledProperty = boolean(true)
    var queueLoopEnabled by queueLoopEnabledProperty
    val skipSilenceProperty = boolean(false)
    var skipSilence by skipSilenceProperty
    val volumeNormalizationProperty = boolean(false)
    var volumeNormalization by volumeNormalizationProperty
    val volumeNormalizationBaseGainProperty = float(5.00f)
    var volumeNormalizationBaseGain by volumeNormalizationBaseGainProperty
    val bassBoostProperty = boolean(false)
    var bassBoost by bassBoostProperty
    val bassBoostLevelProperty = int(5)
    var bassBoostLevel by bassBoostLevelProperty
    val resumePlaybackWhenDeviceConnectedProperty = boolean(false)
    var resumePlaybackWhenDeviceConnected by resumePlaybackWhenDeviceConnectedProperty
    val speedProperty = float(1f)
    var speed by speedProperty
    val pitchProperty = float(1f)
    var pitch by pitchProperty
    var minimumSilence by long(2_000_000L)
    var persistentQueue by boolean(true)
    var stopWhenClosed by boolean(false)

    var isShowingLyrics by boolean(false)
    var isShowingSynchronizedLyrics by boolean(false)

    var isShowingPrevButtonCollapsed by boolean(false)
    var horizontalSwipeToClose by boolean(false)
    var horizontalSwipeToRemoveItem by boolean(false)

    var seekBarStyle by enum(SeekBarStyle.Wavy)
    var wavySeekBarQuality by enum(WavySeekBarQuality.Great)
    var showLike by boolean(false)
    var showRemaining by boolean(false)

    var skipOnError by boolean(false)

    enum class SeekBarStyle(val displayName: @Composable () -> String) {
        Static(displayName = { stringResource(R.string.static_seek_bar_name) }),
        Wavy(displayName = { stringResource(R.string.wavy_seek_bar_name) }),
    }

    enum class WavySeekBarQuality(val quality: Float, val displayName: @Composable () -> String) {
        Poor(quality = 50f, displayName = { stringResource(R.string.seek_bar_quality_poor) }),
        Low(quality = 25f, displayName = { stringResource(R.string.seek_bar_quality_low) }),
        Medium(quality = 15f, displayName = { stringResource(R.string.seek_bar_quality_medium) }),
        High(quality = 5f, displayName = { stringResource(R.string.seek_bar_quality_high) }),
        Great(quality = 1f, displayName = { stringResource(R.string.seek_bar_quality_great) }),
        Subpixel(
            quality = 0.5f,
            displayName = { stringResource(R.string.seek_bar_quality_subpixel) },
        ),
    }
}

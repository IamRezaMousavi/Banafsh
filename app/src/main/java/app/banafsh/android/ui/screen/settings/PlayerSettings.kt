package app.banafsh.android.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerServiceBinder
import app.banafsh.android.R
import app.banafsh.android.preference.PlayerPreferences
import app.banafsh.android.ui.component.TextButton
import app.banafsh.android.util.isAtLeastAndroid6

@Composable
fun PlayerSettings(modifier: Modifier = Modifier) = with(PlayerPreferences) {
    val binder = LocalPlayerServiceBinder.current

    SettingsCategoryScreen(title = stringResource(R.string.player), modifier = modifier) {
        SettingsGroup(title = stringResource(R.string.player)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.persistent_queue),
                text = stringResource(R.string.persistent_queue_description),
                isChecked = persistentQueue,
                onCheckedChange = { persistentQueue = it },
            )

            if (isAtLeastAndroid6)
                SwitchSettingsEntry(
                    title = stringResource(R.string.resume_playback),
                    text = stringResource(R.string.resume_playback_description),
                    isChecked = resumePlaybackWhenDeviceConnected,
                    onCheckedChange = {
                        resumePlaybackWhenDeviceConnected = it
                    },
                )

            SwitchSettingsEntry(
                title = stringResource(R.string.stop_when_closed),
                text = stringResource(R.string.stop_when_closed_description),
                isChecked = stopWhenClosed,
                onCheckedChange = { stopWhenClosed = it },
            )

            SwitchSettingsEntry(
                title = stringResource(R.string.skip_on_error),
                text = stringResource(R.string.skip_on_error_description),
                isChecked = skipOnError,
                onCheckedChange = { skipOnError = it },
            )
        }

        SettingsGroup(title = stringResource(R.string.audio)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.skip_silence),
                text = stringResource(R.string.skip_silence_description),
                isChecked = skipSilence,
                onCheckedChange = {
                    skipSilence = it
                },
            )

            AnimatedVisibility(visible = skipSilence) {
                val initialValue by remember { derivedStateOf { minimumSilence.toFloat() / 1000L } }
                var newValue by remember(initialValue) { mutableFloatStateOf(initialValue) }
                var changed by rememberSaveable { mutableStateOf(false) }

                Column {
                    SliderSettingsEntry(
                        title = stringResource(R.string.minimum_silence_length),
                        text = stringResource(R.string.minimum_silence_length_description),
                        state = newValue,
                        onSlide = { newValue = it },
                        onSlideComplete = {
                            minimumSilence = newValue.toLong() * 1000L
                            changed = true
                        },
                        toDisplay = { stringResource(R.string.format_ms, it.toLong()) },
                        range = 1.00f..2000.000f,
                    )

                    AnimatedVisibility(visible = changed) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SettingsDescription(
                                text = stringResource(R.string.minimum_silence_length_warning),
                                important = true,
                                modifier = Modifier.weight(2f),
                            )
                            TextButton(
                                text = stringResource(R.string.restart_service),
                                onClick = {
                                    binder?.restartForegroundOrStop()?.let { changed = false }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 24.dp),
                            )
                        }
                    }
                }
            }

            SwitchSettingsEntry(
                title = stringResource(R.string.bass_boost),
                text = stringResource(R.string.bass_boost_description),
                isChecked = bassBoost,
                onCheckedChange = { bassBoost = it },
            )

            AnimatedVisibility(visible = bassBoost) {
                var newValue by remember(bassBoostLevel) { mutableFloatStateOf(bassBoostLevel.toFloat()) }

                SliderSettingsEntry(
                    title = stringResource(R.string.bass_boost_level),
                    text = stringResource(R.string.bass_boost_level_description),
                    state = newValue,
                    onSlide = { newValue = it },
                    onSlideComplete = { bassBoostLevel = newValue.toInt() },
                    toDisplay = { (it * 1000f).toInt().toString() },
                    range = 0f..1f,
                )
            }
        }
    }
}

package app.banafsh.android.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.banafsh.android.R
import app.banafsh.android.preference.AppearancePreferences
import app.banafsh.android.ui.theme.ColorMode
import app.banafsh.android.ui.theme.ColorSource
import app.banafsh.android.ui.theme.Darkness

@Composable
fun AppearanceSettings() =
    with(AppearancePreferences) {
        val isDark = isSystemInDarkTheme()
        SettingsCategoryScreen(title = stringResource(R.string.appearance)) {
            SettingsGroup(title = stringResource(R.string.colors)) {
                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.color_source),
                    selectedValue = colorSource,
                    onValueSelect = { colorSource = it },
                    valueText = { it.nameLocalized },
                )
                EnumValueSelectorSettingsEntry(
                    title = stringResource(R.string.color_mode),
                    selectedValue = colorMode,
                    onValueSelect = { colorMode = it },
                    valueText = { it.nameLocalized },
                )
                AnimatedVisibility(visible = colorMode == ColorMode.Dark || (colorMode == ColorMode.System && isDark)) {
                    EnumValueSelectorSettingsEntry(
                        title = stringResource(R.string.darkness),
                        selectedValue = darkness,
                        onValueSelect = { darkness = it },
                        valueText = { it.nameLocalized },
                    )
                }
            }
            SettingsGroup(title = stringResource(R.string.songs)) {
                SwitchSettingsEntry(
                    title = stringResource(R.string.swipe_to_hide_song),
                    text = stringResource(R.string.swipe_to_hide_song_description),
                    isChecked = swipeToHideSong,
                    onCheckedChange = { swipeToHideSong = it },
                )
            }
        }
    }

val ColorSource.nameLocalized
    @Composable get() =
        stringResource(
            when (this) {
                ColorSource.Default -> R.string.color_source_default
                ColorSource.Dynamic -> R.string.color_source_dynamic
                ColorSource.MaterialYou -> R.string.color_source_material_you
            },
        )

val ColorMode.nameLocalized
    @Composable get() =
        stringResource(
            when (this) {
                ColorMode.System -> R.string.color_mode_system
                ColorMode.Light -> R.string.color_mode_light
                ColorMode.Dark -> R.string.color_mode_dark
            },
        )

val Darkness.nameLocalized
    @Composable get() =
        stringResource(
            when (this) {
                Darkness.Normal -> R.string.darkness_normal
                Darkness.AMOLED -> R.string.darkness_amoled
                Darkness.PureBlack -> R.string.darkness_pureblack
            },
        )

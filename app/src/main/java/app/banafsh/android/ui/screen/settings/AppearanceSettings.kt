package app.banafsh.android.ui.screen.settings

import androidx.compose.runtime.Composable
import app.banafsh.android.preference.AppearancePreferences

@Composable
fun AppearanceSettings() =
    with(AppearancePreferences) {
        SettingsCategoryScreen(title = "Appearance") {
            SettingsGroup(title = "Songs") {
                SwitchSettingsEntry(
                    title = "Swipe to hide song",
                    text = "When you swipe a song to the left, it gets removed from the database and cache",
                    isChecked = swipeToHideSong,
                    onCheckedChange = { swipeToHideSong = it },
                )
            }
        }
    }

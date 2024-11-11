package app.banafsh.android.preference

import app.banafsh.android.GlobalPreferencesHolder
import app.banafsh.android.ui.theme.ColorMode
import app.banafsh.android.ui.theme.ColorSource
import app.banafsh.android.ui.theme.Darkness

object AppearancePreferences : GlobalPreferencesHolder() {
    var colorSource by enum(ColorSource.Dynamic)
    var colorMode by enum(ColorMode.System)
    var darkness by enum(Darkness.Normal)
    var swipeToHideSong by boolean(false)
}

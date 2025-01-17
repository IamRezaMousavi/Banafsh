package app.banafsh.android.preference

import app.banafsh.android.GlobalPreferencesHolder

object UIStatePreferences : GlobalPreferencesHolder() {
    var homeScreenTabIndex by int(0)

    var artistScreenTabIndex by int(0)

    var albumScreenTabIndex by int(0)
}

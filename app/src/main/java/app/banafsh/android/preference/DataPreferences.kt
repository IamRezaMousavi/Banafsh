package app.banafsh.android.preference

import app.banafsh.android.GlobalPreferencesHolder
import app.banafsh.android.data.enums.ExoPlayerDiskCacheSize

object DataPreferences : GlobalPreferencesHolder() {
    var exoPlayerDiskCacheMaxSize by enum(ExoPlayerDiskCacheSize.`2GB`)
    var pauseHistory by boolean(false)
    var pausePlaytime by boolean(false)
    var pauseSearchHistory by boolean(false)
    var topListLength by int(50)
    var shouldCacheQuickPicks by boolean(true)
}

package app.banafsh.android.preference

import app.banafsh.android.GlobalPreferencesHolder
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder

object OrderPreferences : GlobalPreferencesHolder() {
    var songSortOrder by enum(SortOrder.Descending)

    var songSortBy by enum(SongSortBy.DateAdded)
}

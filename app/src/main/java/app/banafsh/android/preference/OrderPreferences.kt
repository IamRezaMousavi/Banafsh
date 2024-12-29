package app.banafsh.android.preference

import app.banafsh.android.GlobalPreferencesHolder
import app.banafsh.android.data.enums.AlbumSortBy
import app.banafsh.android.data.enums.ArtistSortBy
import app.banafsh.android.data.enums.PlaylistSortBy
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder

object OrderPreferences : GlobalPreferencesHolder() {
    var songSortOrder by enum(SortOrder.Descending)
    var playlistSortOrder by enum(SortOrder.Descending)
    var albumSortOrder by enum(SortOrder.Descending)
    var artistSortOrder by enum(SortOrder.Descending)

    var songSortBy by enum(SongSortBy.DateAdded)
    var playlistSortBy by enum(PlaylistSortBy.DateAdded)
    var albumSortBy by enum(AlbumSortBy.DateAdded)
    var artistSortBy by enum(ArtistSortBy.DateAdded)
}

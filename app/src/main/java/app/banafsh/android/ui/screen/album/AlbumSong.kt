package app.banafsh.android.ui.screen.album

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.banafsh.android.R
import app.banafsh.android.db.Database
import app.banafsh.android.preference.OrderPreferences
import app.banafsh.android.ui.screen.home.HomeSong

@Composable
fun AlbumSong(albumId: String) = with(OrderPreferences) {
    HomeSong(
        songProvider = {
            Database.albumSongs(albumId)
        },
        sortBy = songSortBy,
        setSortBy = { songSortBy = it },
        sortOrder = songSortOrder,
        setSortOrder = { songSortOrder = it },
        title = stringResource(R.string.songs),
    )
}

package app.banafsh.android.ui.screen.artist

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.banafsh.android.R
import app.banafsh.android.db.Database
import app.banafsh.android.preference.OrderPreferences
import app.banafsh.android.ui.component.SongList

@Composable
fun ArtistSong(artistId: String) = with(OrderPreferences) {
    SongList(
        songProvider = {
            Database.artistSongs(artistId)
        },
        sortBy = songSortBy,
        setSortBy = { songSortBy = it },
        sortOrder = songSortOrder,
        setSortOrder = { songSortOrder = it },
        title = stringResource(R.string.songs),
    )
}

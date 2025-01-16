package app.banafsh.android.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import app.banafsh.android.R
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.Screen
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.util.HasPermissions

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(
        topIconButtonId = R.drawable.settings,
        onTopIconButtonClick = {
            navController.navigate(route = Screen.Settings.route)
        },
        tabIndex = UIStatePreferences.homeScreenTabIndex,
        onTabChange = { UIStatePreferences.homeScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, stringResource(R.string.songs), R.drawable.musical_note)
            item(1, stringResource(R.string.quick_picks), R.drawable.sparkles)
            item(2, stringResource(R.string.discover), R.drawable.global)
            item(3, stringResource(R.string.playlists), R.drawable.playlist)
            item(4, stringResource(R.string.artists), R.drawable.person)
            item(5, stringResource(R.string.albums), R.drawable.disc)
        },
        modifier = modifier,
    ) { currentTabIndex ->
        saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
            HasPermissions {
                when (currentTabIndex) {
                    in 0..3 -> HomeLocalSong()
                    4 -> HomeArtists(
                        onArtistClick = { artist ->
                            navController.navigate(Screen.Artist.route + "/${artist.id}")
                        },
                    )
                    5 -> HomeAlbums(
                        onAlbumClick = { album ->
                            navController.navigate(Screen.Album.route + "/${album.id}")
                        },
                    )
                }
            }
        }
    }
}

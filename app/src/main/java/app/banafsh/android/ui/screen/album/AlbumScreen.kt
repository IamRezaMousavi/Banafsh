package app.banafsh.android.ui.screen.album

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import app.banafsh.android.R
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.component.Scaffold

@Composable
fun AlbumScreen(albumId: String, navController: NavController, modifier: Modifier = Modifier) {
    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(
        topIconButtonId = R.drawable.chevron_back,
        onTopIconButtonClick = {
            navController.popBackStack()
        },
        tabIndex = UIStatePreferences.artistScreenTabIndex,
        onTabChange = { UIStatePreferences.artistScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, stringResource(R.string.songs), R.drawable.musical_note)
        },
        modifier = modifier,
    ) { currentTabIndex ->
        saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
            when (currentTabIndex) {
                0 -> AlbumSong(albumId)
            }
        }
    }
}

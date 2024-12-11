package app.banafsh.android.ui.screen.home

import androidx.compose.runtime.Composable
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
    Scaffold(
        topIconButtonId = R.drawable.settings,
        onTopIconButtonClick = {
            navController.navigate(route = Screen.Settings.route)
        },
        tabIndex = UIStatePreferences.homeScreenTabIndex,
        onTabChange = { UIStatePreferences.homeScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, stringResource(R.string.songs), R.drawable.musical_note)
            item(1, stringResource(R.string.discover), R.drawable.global)
            item(2, stringResource(R.string.artists), R.drawable.person)
            item(3, stringResource(R.string.albums), R.drawable.disc)
        },
        modifier = modifier,
    ) {
        HasPermissions {
            HomeLocalSong()
        }
    }
}

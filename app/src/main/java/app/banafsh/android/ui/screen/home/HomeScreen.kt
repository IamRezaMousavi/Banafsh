package app.banafsh.android.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            item(0, "Hello", R.drawable.global)
            item(1, "Reza", R.drawable.musical_note)
            item(2, "Ali", R.drawable.person)
            item(3, "Mohammad", R.drawable.disc)
        },
        modifier = modifier,
    ) {
        HasPermissions {
            HomeLocalSong()
        }
    }
}

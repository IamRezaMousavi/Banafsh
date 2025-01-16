package app.banafsh.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.banafsh.android.ui.screen.album.AlbumScreen
import app.banafsh.android.ui.screen.artist.ArtistScreen
import app.banafsh.android.ui.screen.home.HomeScreen
import app.banafsh.android.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home_screen")

    data object Artist : Screen("artist_screen")

    data object Album : Screen("album_screen")

    data object Settings : Screen("settings_screen")
}

@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.Artist.route + "/{artistId}",
            arguments = listOf(
                navArgument(name = "artistId") {
                    type = NavType.StringType
                },
            ),
        ) { backstackEntry ->
            ArtistScreen(
                artistId = backstackEntry.arguments?.getString("artistId").orEmpty(),
                navController = navController,
            )
        }

        composable(
            route = Screen.Album.route + "/{albumId}",
            arguments = listOf(
                navArgument(name = "albumId") {
                    type = NavType.StringType
                },
            ),
        ) { backstackEntry ->
            AlbumScreen(
                albumId = backstackEntry.arguments?.getString("albumId").orEmpty(),
                navController = navController,
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

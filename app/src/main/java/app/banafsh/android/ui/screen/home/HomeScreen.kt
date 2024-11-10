package app.banafsh.android.ui.screen.home

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.model.Song
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.Screen
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.ui.item.SongItem

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topIconButtonId = R.drawable.ic_launcher_foreground,
        onTopIconButtonClick = {
            navController.navigate(route = Screen.Settings.route)
        },
        tabIndex = UIStatePreferences.homeScreenTabIndex,
        onTabChange = { UIStatePreferences.homeScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, "Hello", R.drawable.ic_launcher_foreground)
            item(1, "Reza", R.drawable.ic_launcher_foreground)
            item(2, "Ali", R.drawable.ic_launcher_foreground)
            item(3, "Mohammad", R.drawable.ic_launcher_foreground)
        },
        modifier = modifier,
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues(),
        ) {
            item(
                key = "header",
                contentType = 0,
            ) {
                Header(title = "Main") {
                    Text(
                        "A",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "B",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "C",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(150, { it.toString() }) {
                val song = Song(it.toString(), "Number $it", "Reza Mousavi", 10245)
                SongItem(song)
            }
        }
    }
}

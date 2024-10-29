package app.banafsh.android.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.model.Song
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.ui.component.Switch2
import app.banafsh.android.ui.item.SongItem

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topIconButtonId = R.drawable.ic_launcher_foreground,
        onTopIconButtonClick = {},
        tabIndex = UIStatePreferences.homeScreenTabIndex,
        onTabChange = { UIStatePreferences.homeScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, "Hello", R.drawable.ic_launcher_foreground)
            item(1, "Reza", R.drawable.ic_launcher_foreground)
            item(2, "Ali", R.drawable.ic_launcher_foreground)
            item(3, "Mohammad", R.drawable.ic_launcher_foreground)
        },
        modifier = modifier
    ) {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    titleContent = {
                        Text(
                            "Main",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actionsContent = {
                        Text(
                            "A",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "B",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "C",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                var text by remember { mutableStateOf("Hello") }
                var checked by remember { mutableStateOf(true) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(targetState = text, label = "") { text ->
                        Text(
                            text,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable { checked = !checked },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Click to activate",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Don't Click",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable { checked = !checked },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            "Click to activate",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Don't Click",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch2(
                        isChecked = checked,
                    )
                }

                Button(
                    onClick = {
                        val length = 5
                        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
                        text = (1..length)
                            .map { allowedChars.random() }
                            .joinToString("")
                    }
                ) {
                    Text("Click Me")
                }

            }
            items(150, { it.toString() }) {
                val song = Song(it.toString(), "Number $it", "Reza Mousavi", 10245)
                SongItem(song)
            }

        }
    }
}
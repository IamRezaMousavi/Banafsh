package app.banafsh.android.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.NavController
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.Screen
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.ui.component.Switch2

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topIconButtonId = R.drawable.ic_launcher_foreground,
        onTopIconButtonClick = {
            navController.navigate(route = Screen.Home.route)
        },
        tabIndex = UIStatePreferences.homeScreenTabIndex,
        onTabChange = { UIStatePreferences.homeScreenTabIndex = it },
        tabColumnContent = { item ->
            item(0, "UI", R.drawable.ic_launcher_foreground)
            item(1, "DB", R.drawable.ic_launcher_foreground)
            item(2, "About", R.drawable.ic_launcher_foreground)
        },
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        LocalPlayerAwareWindowInsets.current
                            .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                            .asPaddingValues(),
                    ),
        ) {
            Header(
                titleContent = {
                    Text(
                        "Settings",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                actionsContent = {
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
                },
            )

            var text by remember { mutableStateOf("Hello") }
            var checked by remember { mutableStateOf(true) }

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedContent(targetState = text, label = "") { text ->
                    Text(
                        text,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable { checked = !checked },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        "Click to activate",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Don't Click",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                    },
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable { checked = !checked },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        "Click to activate",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Don't Click",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text =
                        (1..length)
                            .map { allowedChars.random() }
                            .joinToString("")
                },
            ) {
                Text("Click Me")
            }
        }
    }
}

package app.banafsh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.ui.component.Switch2
import app.banafsh.android.ui.theme.BanafshTheme

val LocalPlayerAwareWindowInsets =
    compositionLocalOf<WindowInsets> { error("No player insets provided") }

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanafshTheme {
                val density = LocalDensity.current
                val windowsInsets = WindowInsets.systemBars
                val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

                val imeVisible = WindowInsets.isImeVisible
                val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
                val animatedBottomDp by animateDpAsState(
                    targetValue = if (imeVisible) 0.dp else bottomDp,
                    label = ""
                )

                val playerAwareWindowInsets = remember(
                    bottomDp,
                    animatedBottomDp,
                    imeVisible,
                    imeBottomDp
                ) {
                    windowsInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .add(WindowInsets(bottom = 6.dp))
                }

                CompositionLocalProvider(
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                ) {
                    var tabIndex by remember { mutableIntStateOf(0) }
                    Scaffold(
                        topIconButtonId = R.drawable.ic_launcher_foreground,
                        onTopIconButtonClick = {},
                        tabIndex = tabIndex,
                        onTabChange = { tabIndex = it },
                        tabColumnContent = { item ->
                            item(0, "Hello", R.drawable.ic_launcher_foreground)
                            item(1, "Reza", R.drawable.ic_launcher_foreground)
                            item(2, "Ali", R.drawable.ic_launcher_foreground)
                            item(3, "Mohammad", R.drawable.ic_launcher_foreground)
                        }
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
                                    Text(
                                        text,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Start
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
                                Text(
                                    it.toString(),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}


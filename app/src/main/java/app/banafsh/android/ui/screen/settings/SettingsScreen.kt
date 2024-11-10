package app.banafsh.android.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.preference.UIStatePreferences
import app.banafsh.android.ui.Screen
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.Scaffold
import app.banafsh.android.ui.component.Switch

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
        AppearanceSettings()
    }
}

@Composable
fun SettingsGroupSpacer(modifier: Modifier = Modifier) = Spacer(modifier = modifier.height(24.dp))

@Composable
fun SettingsCategoryScreen(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
                .padding(
                    LocalPlayerAwareWindowInsets.current
                        .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                        .asPaddingValues(),
                ),
    ) {
        Header(title = title) {
            description?.let { description ->
                Text(
                    description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
                SettingsGroupSpacer()
            }
        }
        content()
    }
}

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) = Column(modifier = modifier) {
    Text(
        title.uppercase(),
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp),
    )

    description?.let {
        Text(
            description,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier =
                Modifier
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        )
    }

    content()

    SettingsGroupSpacer()
}

@Composable
fun SettingsEntry(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
) = Row(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier =
        modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(16.dp)
            .fillMaxWidth(),
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        text?.let {
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    trailingContent?.invoke()
}

@Composable
fun SwitchSettingsEntry(
    title: String,
    text: String?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) = SettingsEntry(
    modifier = modifier,
    title = title,
    text = text,
    onClick = { onCheckedChange(!isChecked) },
    isEnabled = isEnabled,
) {
    Switch(isChecked = isChecked)
}

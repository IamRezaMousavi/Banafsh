package app.banafsh.android.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.banafsh.android.MainViewModel
import app.banafsh.android.R
import app.banafsh.android.TempDatabase
import app.banafsh.android.lib.core.ui.LocalAppearance
import app.banafsh.android.permissions
import app.banafsh.android.preferences.OrderPreferences
import app.banafsh.android.ui.components.themed.SecondaryTextButton
import app.banafsh.android.ui.screens.Route
import app.banafsh.android.utils.medium
import kotlinx.coroutines.flow.map

@Route
@Composable
fun HomeLocalSongs(onSearchClick: () -> Unit) = with(OrderPreferences) {
    val context = LocalContext.current
    val (_, typography) = LocalAppearance.current

    val vm = viewModel<MainViewModel>()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { vm.setHasPermissions(it.values.all { it }) }
    )

    if (vm.hasPermissions.value)
        HomeSongs(
            onSearchClick = onSearchClick,
            songProvider = {
                TempDatabase.songs(
                    sortBy = localSongSortBy,
                    sortOrder = localSongSortOrder
                ).map { songs ->
                    songs.filter { it.durationText != "0:00" }
                }
            },
            sortBy = localSongSortBy,
            setSortBy = { localSongSortBy = it },
            sortOrder = localSongSortOrder,
            setSortOrder = { localSongSortOrder = it },
            title = stringResource(R.string.songs)
        )
    else {
        LaunchedEffect(Unit) { launcher.launch(permissions) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = stringResource(R.string.media_permission_declined),
                modifier = Modifier.fillMaxWidth(0.75f),
                style = typography.m.medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryTextButton(
                text = stringResource(R.string.open_settings),
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            )
        }
    }
}

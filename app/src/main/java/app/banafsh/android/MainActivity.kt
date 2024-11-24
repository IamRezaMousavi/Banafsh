package app.banafsh.android

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.data.provider.musicFilesAsFlow
import app.banafsh.android.service.PlayerService
import app.banafsh.android.ui.NavigationStack
import app.banafsh.android.ui.component.TextButton
import app.banafsh.android.ui.theme.BanafshTheme
import app.banafsh.android.util.hasPermissions
import app.banafsh.android.util.isAtLeastAndroid10
import app.banafsh.android.util.isAtLeastAndroid13
import app.banafsh.android.util.isAtLeastAndroid8
import app.banafsh.android.util.isCompositionLaunched
import kotlinx.coroutines.flow.collect

val permissions =
    when {
        isAtLeastAndroid13 -> {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        }
        isAtLeastAndroid10 -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }

val LocalPlayerAwareWindowInsets =
    compositionLocalOf<WindowInsets> { error("No player insets provided") }

class MainActivity : ComponentActivity() {
    var isServiceRunning = false
    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, PlayerService::class.java)
            if (isAtLeastAndroid8) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }

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
                    label = "",
                )

                val playerAwareWindowInsets =
                    remember(
                        bottomDp,
                        animatedBottomDp,
                        imeVisible,
                        imeBottomDp,
                    ) {
                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(bottom = 6.dp))
                    }

                CompositionLocalProvider(
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                ) {
                    val context = LocalContext.current

                    var hasPermission by remember(isCompositionLaunched()) {
                        mutableStateOf(context.applicationContext.hasPermissions(permissions))
                    }

                    val launcher =
                        rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions(),
                            onResult = { result -> hasPermission = result.values.all { it } },
                        )

                    LaunchedEffect(hasPermission) {
                        if (hasPermission) context.musicFilesAsFlow().collect()
                    }

                    if (hasPermission) {
                        NavigationStack()
                    } else {
                        LaunchedEffect(Unit) { launcher.launch(permissions) }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            BasicText(
                                text = stringResource(R.string.media_permission_declined),
                                modifier = Modifier.fillMaxWidth(0.75f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                text = stringResource(R.string.open_settings),
                                onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

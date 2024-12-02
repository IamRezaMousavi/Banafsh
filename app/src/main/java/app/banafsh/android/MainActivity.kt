package app.banafsh.android

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import app.banafsh.android.service.PlayerService
import app.banafsh.android.ui.NavigationStack
import app.banafsh.android.ui.component.rememberBottomSheetState
import app.banafsh.android.ui.screen.player.Player
import app.banafsh.android.ui.theme.BanafshTheme
import app.banafsh.android.ui.theme.Dimensions
import app.banafsh.android.util.DisposableListener
import app.banafsh.android.util.intent
import app.banafsh.android.util.isAtLeastAndroid10
import app.banafsh.android.util.isAtLeastAndroid13

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
val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }

class MainViewModel : ViewModel() {
    var binder: PlayerService.Binder? by mutableStateOf(null)
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) vm.binder = service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            vm.binder = null
            // Try to rebind, otherwise fail
            unbindService(this)
            bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(intent<PlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BanafshTheme {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

                    val imeVisible = WindowInsets.isImeVisible
                    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
                    val animatedBottomDp by animateDpAsState(
                        targetValue = if (imeVisible) 0.dp else bottomDp,
                        label = "",
                    )

                    val playerBottomSheetState = rememberBottomSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = Dimensions.items.collapsedPlayerHeight + bottomDp,
                        expandedBound = maxHeight,
                    )

                    val playerAwareWindowInsets = remember(
                        bottomDp,
                        animatedBottomDp,
                        playerBottomSheetState.value,
                        imeVisible,
                        imeBottomDp,
                    ) {
                        val bottom =
                            if (imeVisible) imeBottomDp.coerceAtLeast(playerBottomSheetState.value)
                            else playerBottomSheetState.value.coerceIn(
                                animatedBottomDp..playerBottomSheetState.collapsedBound,
                            )

                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(bottom = bottom))
                    }

                    CompositionLocalProvider(
                        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        LocalPlayerServiceBinder provides vm.binder,
                    ) {
                        Box {
                            NavigationStack()
                        }

                        Player(
                            layoutState = playerBottomSheetState,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }

                    vm.binder?.player?.DisposableListener {
                        object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = when {
                                mediaItem == null -> playerBottomSheetState.dismissSoft()

                                reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED &&
                                    mediaItem.mediaMetadata.extras?.getBoolean("isFromPersistentQueue") != true
                                -> playerBottomSheetState.expandSoft()

                                playerBottomSheetState.dismissed -> playerBottomSheetState.collapseSoft()

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }
}

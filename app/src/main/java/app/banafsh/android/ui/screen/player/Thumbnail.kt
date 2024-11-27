package app.banafsh.android.ui.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerServiceBinder
import app.banafsh.android.R
import app.banafsh.android.ui.modifier.onSwipe
import app.banafsh.android.util.forceSeekToNext
import app.banafsh.android.util.forceSeekToPrevious
import app.banafsh.android.util.toast
import app.banafsh.android.util.windowState
import coil.compose.AsyncImage

@Composable
fun Thumbnail(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    val (nullableWindow, error) = windowState()
    val window = nullableWindow ?: return

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            val duration = 500
            val sizeTransform = SizeTransform(clip = false) { _, _ ->
                tween(durationMillis = duration, delayMillis = duration)
            }

            val direction =
                if (targetState.firstPeriodIndex > initialState.firstPeriodIndex) Left else Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(direction, tween(duration)) +
                    fadeIn(tween(duration)) +
                    scaleIn(tween(duration), 0.85f),
                initialContentExit = slideOutOfContainer(direction, tween(duration)) +
                    fadeOut(tween(duration)) +
                    scaleOut(tween(duration), 0.85f),
                sizeTransform = sizeTransform,
            )
        },
        modifier = modifier.onSwipe(
            onSwipeLeft = {
                binder.player.forceSeekToNext()
            },
            onSwipeRight = {
                binder.player.seekToDefaultPosition()
                binder.player.forceSeekToPrevious()
            },
        ),
        contentAlignment = Alignment.Center,
        label = "",
    ) { currentWindow ->
        val shadowElevation by animateDpAsState(
            targetValue = if (window == currentWindow) 8.dp else 0.dp,
            animationSpec = tween(500),
            label = "",
        )
        val blurRadius by animateDpAsState(
            targetValue = if (error != null) 8.dp else 0.dp,
            animationSpec = tween(500),
            label = "",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = shadowElevation,
                    shape = MaterialTheme.shapes.small,
                    clip = false,
                )
                .clip(MaterialTheme.shapes.small),
        ) {
            var height by remember { mutableIntStateOf(0) }

            AsyncImage(
                model = currentWindow.mediaItem.mediaMetadata.artworkUri,
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { context.toast("show lyrics") },
                            onLongPress = { context.toast("Long Press") },
                        )
                    }
                    .fillMaxWidth()
                    .animateContentSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .let {
                        if (blurRadius == 0.dp) it else it.blur(radius = blurRadius)
                    }
                    .onGloballyPositioned {
                        height = it.size.height
                    },
            )

            PlaybackError(
                isDisplayed = error != null,
                messageProvider = {
                    stringResource(R.string.error_local_music_deleted)
                },
                onDismiss = player::prepare,
            )
        }
    }
}

package app.banafsh.android.ui.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import app.banafsh.android.LocalPlayerServiceBinder
import app.banafsh.android.R
import app.banafsh.android.data.model.ui.toUiMedia
import app.banafsh.android.preference.PlayerPreferences
import app.banafsh.android.ui.component.BottomSheet
import app.banafsh.android.ui.component.BottomSheetState
import app.banafsh.android.ui.component.IconButton
import app.banafsh.android.ui.component.rememberBottomSheetState
import app.banafsh.android.ui.theme.Dimensions
import app.banafsh.android.util.DisposableListener
import app.banafsh.android.util.forceSeekToNext
import app.banafsh.android.util.forceSeekToPrevious
import app.banafsh.android.util.positionAndDurationState
import app.banafsh.android.util.shouldBePlaying
import coil.compose.AsyncImage
import kotlin.math.absoluteValue

@Composable
fun Player(layoutState: BottomSheetState, modifier: Modifier = Modifier) = with(PlayerPreferences) {
    val colorPalette = MaterialTheme.colorScheme
    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return@with

    var nullableMediaItem by remember {
        mutableStateOf(
            value = binder.player.currentMediaItem,
            policy = neverEqualPolicy(),
        )
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return
    val positionAndDuration by binder.player.positionAndDurationState()

    val horizontalBottomPaddingValues = WindowInsets.systemBars
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        .asPaddingValues()

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        onDismiss = {
            binder.player.stop()
            binder.player.clearMediaItems()
        },
        collapsedContent = { innerModifier ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(colorPalette.surfaceContainer)
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            color = colorPalette.primaryContainer,
                            topLeft = Offset.Zero,
                            size = Size(
                                width = positionAndDuration.first.toFloat() /
                                    positionAndDuration.second.absoluteValue * size.width,
                                height = size.height,
                            ),
                        )
                    }
                    .then(innerModifier)
                    .padding(horizontalBottomPaddingValues),
            ) {
                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(Dimensions.items.collapsedPlayerHeight),
                ) {
                    AsyncImage(
                        model = mediaItem.mediaMetadata.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(colorPalette.surface)
                            .size(48.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(Dimensions.items.collapsedPlayerHeight)
                        .weight(1f),
                ) {
                    AnimatedContent(
                        targetState = mediaItem.mediaMetadata.title?.toString().orEmpty(),
                        label = "",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    AnimatedVisibility(visible = mediaItem.mediaMetadata.artist != null) {
                        AnimatedContent(
                            targetState = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
                            label = "",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                        ) { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(Dimensions.items.collapsedPlayerHeight),
                ) {
                    AnimatedVisibility(visible = isShowingPrevButtonCollapsed) {
                        IconButton(
                            icon = R.drawable.play_skip_back,
                            color = colorPalette.onSurface,
                            onClick = binder.player::forceSeekToPrevious,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .size(20.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    if (shouldBePlaying) binder.player.pause()
                                    else {
                                        if (binder.player.playbackState == Player.STATE_IDLE) binder.player.prepare()
                                        binder.player.play()
                                    }
                                },
                                indication = ripple(bounded = false),
                                interactionSource = remember { MutableInteractionSource() },
                            )
                            .clip(CircleShape),
                    ) {
                        AnimatedPlayPauseButton(
                            playing = shouldBePlaying,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .size(23.dp),
                        )
                    }

                    IconButton(
                        icon = R.drawable.play_skip_forward,
                        color = colorPalette.onSurface,
                        onClick = binder.player::forceSeekToNext,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))
            }
        },
    ) {
        val playerBottomSheetState = rememberBottomSheetState(
            dismissedBound = 64.dp + horizontalBottomPaddingValues.calculateBottomPadding(),
            expandedBound = layoutState.expandedBound,
        )

        val containerModifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                Brush.verticalGradient(
                    0.5f to colorPalette.surfaceContainer,
                    1f to colorPalette.surface,
                ),
            )
            .padding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            )
            .padding(bottom = playerBottomSheetState.collapsedBound)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = containerModifier
                .padding(20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1.25f),
            ) {
                Thumbnail(Modifier.padding(horizontal = 32.dp, vertical = 8.dp))
            }

            val (position, duration) = positionAndDuration
            Controls(
                media = mediaItem.toUiMedia(duration),
                binder = binder,
                shouldBePlaying = shouldBePlaying,
                position = position,
                Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

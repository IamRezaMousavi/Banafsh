package app.banafsh.android.ui.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import app.banafsh.android.R
import app.banafsh.android.data.model.Song
import app.banafsh.android.data.model.ui.UiMedia
import app.banafsh.android.db.Database
import app.banafsh.android.db.query
import app.banafsh.android.preference.PlayerPreferences
import app.banafsh.android.service.PlayerService
import app.banafsh.android.ui.component.FadingRow
import app.banafsh.android.ui.component.IconButton
import app.banafsh.android.ui.component.SeekBar
import app.banafsh.android.ui.theme.util.roundedShape
import app.banafsh.android.util.forceSeekToNext
import app.banafsh.android.util.forceSeekToPrevious
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Controls(
    media: UiMedia,
    binder: PlayerService.Binder,
    shouldBePlaying: Boolean,
    position: Long,
    modifier: Modifier = Modifier,
) = with(PlayerPreferences) {
    var likedAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(media) {
        Database
            .likedAt(media.id)
            .distinctUntilChanged()
            .collect { likedAt = it }
    }

    val shouldBePlayingTransition = updateTransition(
        targetState = shouldBePlaying,
        label = "shouldBePlaying",
    )

    val playButtonRadius by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 16.dp else 32.dp },
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = media.title,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(durationMillis = 300, delayMillis = 300),
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 300),
                    )
                },
                label = "",
            ) { title ->
                FadingRow(modifier = Modifier.fillMaxWidth(0.75f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }

            AnimatedContent(
                targetState = media.artist,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(durationMillis = 300, delayMillis = 300),
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 300),
                    )
                },
                label = "",
            ) { title ->
                FadingRow(modifier = Modifier.fillMaxWidth(0.75f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SeekBar(
            binder = binder,
            position = position,
            media = media,
            alwaysShowDuration = true,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                icon = R.drawable.infinite,
                enabled = trackLoopEnabled,
                onClick = { trackLoopEnabled = !trackLoopEnabled },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
            )

            IconButton(
                icon = R.drawable.play_skip_back,
                color = MaterialTheme.colorScheme.onSurface,
                onClick = binder.player::forceSeekToPrevious,
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(playButtonRadius.roundedShape)
                    .clickable {
                        if (shouldBePlaying) binder.player.pause()
                        else {
                            if (binder.player.playbackState == Player.STATE_IDLE) binder.player.prepare()
                            binder.player.play()
                        }
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .size(64.dp),
            ) {
                AnimatedPlayPauseButton(
                    playing = shouldBePlaying,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                icon = R.drawable.play_skip_forward,
                color = MaterialTheme.colorScheme.onSurface,
                onClick = binder.player::forceSeekToNext,
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
            )

            IconButton(
                icon = R.drawable.shuffle,
                enabled = shuffleModeEnabled,
                onClick = { shuffleModeEnabled = !shuffleModeEnabled },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem

                    query {
                        if (
                            Database.like(
                                media.id,
                                if (likedAt == null) System.currentTimeMillis() else null,
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == media.id }
                                ?.let {
                                    Database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .size(20.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                icon = R.drawable.playlist,
                enabled = true,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .size(20.dp),
            )
        }
    }
}

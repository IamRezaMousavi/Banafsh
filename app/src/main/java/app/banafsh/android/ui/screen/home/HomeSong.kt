package app.banafsh.android.ui.screen.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Song
import app.banafsh.android.preference.OrderPreferences
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.HeaderIconButton
import app.banafsh.android.ui.item.SongItem

@Composable
fun HomeSong(modifier: Modifier = Modifier) =
    with(OrderPreferences) {
        val sortOrderIconRotation by animateFloatAsState(
            targetValue = if (songSortOrder == SortOrder.Ascending) 0f else 180f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing),
            label = "",
        )

        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues(),
            modifier = modifier,
        ) {
            item(
                key = "header",
                contentType = 0,
            ) {
                Header(title = "Main") {
                    HeaderIconButton(
                        icon = R.drawable.trending,
                        enabled = songSortBy == SongSortBy.PlayTime,
                        onClick = { songSortBy = SongSortBy.PlayTime },
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        enabled = songSortBy == SongSortBy.Title,
                        onClick = { songSortBy = SongSortBy.Title },
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        enabled = songSortBy == SongSortBy.DateAdded,
                        onClick = { songSortBy = SongSortBy.DateAdded },
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        onClick = { songSortOrder = songSortOrder.not() },
                        modifier = Modifier.graphicsLayer { rotationZ = sortOrderIconRotation },
                    )
                }
            }
            items(150, { it.toString() }) {
                val song = Song(it.toString(), "Number $it", "Reza Mousavi", 10245)
                SongItem(song)
            }
        }
    }

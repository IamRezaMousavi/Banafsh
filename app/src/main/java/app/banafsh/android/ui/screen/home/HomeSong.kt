package app.banafsh.android.ui.screen.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Song
import app.banafsh.android.preference.OrderPreferences.songSortOrder
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.HeaderIconButton
import app.banafsh.android.ui.item.SongItem
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeSong(
    songProvider: () -> Flow<List<Song>>,
    sortBy: SongSortBy,
    setSortBy: (SongSortBy) -> Unit,
    sortOrder: SortOrder,
    setSortOrder: (SortOrder) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    var songs by remember { mutableStateOf(emptyList<Song>()) }

    LaunchedEffect(sortBy, sortOrder, songProvider) {
        songProvider().collect { songs = it.toPersistentList() }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (songSortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "",
    )

    val lazyListState = rememberLazyListState()
    Box(
        modifier =
        modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding =
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues(),
        ) {
            item(
                key = "header",
                contentType = 0,
            ) {
                Header(title = title) {
                    HeaderIconButton(
                        icon = R.drawable.trending,
                        enabled = sortBy == SongSortBy.PlayTime,
                        onClick = { setSortBy(SongSortBy.PlayTime) },
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        enabled = sortBy == SongSortBy.Title,
                        onClick = { setSortBy(SongSortBy.Title) },
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        enabled = sortBy == SongSortBy.DateAdded,
                        onClick = { setSortBy(SongSortBy.DateAdded) },
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        onClick = { setSortOrder(!sortOrder) },
                        modifier = Modifier.graphicsLayer { rotationZ = sortOrderIconRotation },
                    )
                }
            }
            items(songs, key = { song -> song.id }) { song ->
                SongItem(song)
            }
        }
    }
}

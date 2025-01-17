package app.banafsh.android.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.LocalPlayerServiceBinder
import app.banafsh.android.R
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Song
import app.banafsh.android.preference.OrderPreferences.songSortOrder
import app.banafsh.android.preference.PlayerPreferences
import app.banafsh.android.ui.item.SongItem
import app.banafsh.android.util.asMediaItem
import app.banafsh.android.util.forcePlayAtIndex
import kotlin.random.Random
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow

@Composable
fun SongList(
    songProvider: () -> Flow<List<Song>>,
    sortBy: SongSortBy,
    setSortBy: (SongSortBy) -> Unit,
    sortOrder: SortOrder,
    setSortOrder: (SortOrder) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    val binder = LocalPlayerServiceBinder.current

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var filter: String? by remember { mutableStateOf(null) }

    var songs by remember { mutableStateOf(emptyList<Song>()) }
    val filteredItems by remember {
        derivedStateOf {
            filter?.lowercase()?.ifBlank { null }?.let { f ->
                songs.filter {
                    f in it.title.lowercase() || f in it.artist?.lowercase().orEmpty()
                }.sortedBy { it.title }
            } ?: songs
        }
    }

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
                    var searching by rememberSaveable { mutableStateOf(false) }
                    AnimatedContent(
                        targetState = searching,
                        label = "",
                    ) { state ->
                        if (state) {
                            val focusRequester = remember { FocusRequester() }

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }

                            TextField(
                                value = filter.orEmpty(),
                                onValueChange = { filter = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    if (filter.isNullOrBlank()) filter = ""
                                    focusManager.clearFocus()
                                }),
                                hintText = stringResource(R.string.filter_placeholder),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (!it.hasFocus) {
                                            keyboardController?.hide()
                                            if (filter?.isBlank() == true) {
                                                filter = null
                                                searching = false
                                            }
                                        }
                                    },
                            )
                        } else Row(verticalAlignment = Alignment.CenterVertically) {
                            HeaderIconButton(
                                icon = R.drawable.search,
                                onClick = { searching = true },
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (songs.isNotEmpty())
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.song_count_plural,
                                        songs.size,
                                        songs.size,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

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
            items(
                items = filteredItems,
                key = { song -> song.id },
            ) { song ->
                SongItem(
                    song,
                    modifier = Modifier
                        .clickable {
                            keyboardController?.hide()
                            binder?.player?.forcePlayAtIndex(
                                songs.map(Song::asMediaItem),
                                songs.indexOf(song),
                            )
                            binder?.player?.play()
                        }.animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            icon = R.drawable.shuffle,
            onClick = {
                keyboardController?.hide()
                val randIndex = Random.nextInt(songs.size)
                binder?.player?.forcePlayAtIndex(
                    songs.map(Song::asMediaItem),
                    randIndex,
                )
                binder?.player?.play()
                PlayerPreferences.shuffleModeEnabled = true
            },
        )
    }
}

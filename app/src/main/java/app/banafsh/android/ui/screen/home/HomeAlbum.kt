package app.banafsh.android.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import app.banafsh.android.R
import app.banafsh.android.data.enums.AlbumSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Album
import app.banafsh.android.db.Database
import app.banafsh.android.preference.OrderPreferences
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.HeaderIconButton
import app.banafsh.android.ui.component.TextField
import app.banafsh.android.ui.item.AlbumItem
import app.banafsh.android.ui.theme.Dimensions

@Composable
fun HomeAlbum(onAlbumClick: (Album) -> Unit, modifier: Modifier = Modifier) = with(OrderPreferences) {
    val colorPalette = MaterialTheme.colorScheme

    var items by remember { mutableStateOf(emptyList<Album>()) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var filter: String? by remember { mutableStateOf(null) }

    val filteredItems by remember {
        derivedStateOf {
            filter?.lowercase()?.ifBlank { null }?.let { f ->
                items
                    .filter { f in it.title?.lowercase().orEmpty() || f in it.authors?.lowercase().orEmpty() }
                    .sortedBy { it.title }
            } ?: items
        }
    }

    LaunchedEffect(albumSortBy, albumSortOrder) {
        Database.albums(albumSortBy, albumSortOrder).collect { items = it }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (albumSortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "",
    )

    val lazyGridState = rememberLazyGridState()

    Box(modifier = modifier) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.items.verticalPadding * 2),
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.items.verticalPadding * 2),
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimensions.items.verticalPadding * 2,
                alignment = Alignment.CenterHorizontally,
            ),
            modifier = Modifier
                .background(colorPalette.surface)
                .fillMaxSize(),
        ) {
            item(
                key = "header",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Header(title = stringResource(R.string.albums)) {
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

                            if (items.isNotEmpty())
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.album_count_plural,
                                        items.size,
                                        items.size,
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
                        icon = R.drawable.calendar,
                        enabled = albumSortBy == AlbumSortBy.Year,
                        onClick = { albumSortBy = AlbumSortBy.Year },
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        enabled = albumSortBy == AlbumSortBy.Title,
                        onClick = { albumSortBy = AlbumSortBy.Title },
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        enabled = albumSortBy == AlbumSortBy.DateAdded,
                        onClick = { albumSortBy = AlbumSortBy.DateAdded },
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        onClick = { albumSortOrder = !albumSortOrder },
                        modifier = Modifier.graphicsLayer { rotationZ = sortOrderIconRotation },
                    )
                }
            }

            items(
                items = filteredItems,
                key = Album::id,
            ) { album ->
                AlbumItem(
                    album = album,
                    modifier = Modifier
                        .clickable(onClick = { onAlbumClick(album) })
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }
    }
}

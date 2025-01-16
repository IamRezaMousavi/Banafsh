package app.banafsh.android.ui.screen.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.data.enums.AlbumSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Album
import app.banafsh.android.db.Database
import app.banafsh.android.preference.OrderPreferences
import app.banafsh.android.ui.component.FloatingActionsContainerWithScrollToTop
import app.banafsh.android.ui.component.Header
import app.banafsh.android.ui.component.HeaderIconButton
import app.banafsh.android.ui.item.AlbumItem
import app.banafsh.android.ui.theme.Dimensions

@Composable
fun HomeAlbums(onAlbumClick: (Album) -> Unit, modifier: Modifier = Modifier) = with(OrderPreferences) {
    val colorPalette = MaterialTheme.colorScheme

    var items by remember { mutableStateOf(emptyList<Album>()) }

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
                items = items,
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

        FloatingActionsContainerWithScrollToTop(
            lazyGridState = lazyGridState,
            icon = R.drawable.search,
            onClick = { },
        )
    }
}

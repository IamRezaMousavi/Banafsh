package app.banafsh.android.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.data.models.Mood
import app.banafsh.android.persist.persist
import app.banafsh.android.providers.innertube.Innertube
import app.banafsh.android.providers.innertube.models.bodies.BrowseBody
import app.banafsh.android.providers.innertube.requests.BrowseResult
import app.banafsh.android.providers.innertube.requests.browse
import app.banafsh.android.ui.components.ShimmerHost
import app.banafsh.android.ui.components.themed.Header
import app.banafsh.android.ui.components.themed.HeaderPlaceholder
import app.banafsh.android.ui.components.themed.TextPlaceholder
import app.banafsh.android.ui.items.AlbumItem
import app.banafsh.android.ui.items.AlbumItemPlaceholder
import app.banafsh.android.ui.items.ArtistItem
import app.banafsh.android.ui.items.PlaylistItem
import app.banafsh.android.ui.screens.albumRoute
import app.banafsh.android.ui.screens.artistRoute
import app.banafsh.android.ui.screens.playlistRoute
import app.banafsh.android.ui.theme.Dimensions
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.utils.center
import app.banafsh.android.utils.secondary
import app.banafsh.android.utils.semiBold
import com.valentinilk.shimmer.shimmer

internal const val DEFAULT_BROWSE_ID = "FEmusic_moods_and_genres_category"

@Composable
fun MoodList(
    mood: Mood,
    modifier: Modifier = Modifier
) = Column(modifier = modifier) {
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val browseId = mood.browseId ?: DEFAULT_BROWSE_ID
    var moodPage by persist<Result<BrowseResult>>(
        tag = "playlist/$browseId${mood.params?.let { "/$it" }.orEmpty()}"
    )

    LaunchedEffect(Unit) {
        if (moodPage?.isSuccess == true) return@LaunchedEffect

        moodPage = Innertube.browse(BrowseBody(browseId = browseId, params = mood.params))
    }

    val lazyListState = rememberLazyListState()

    val endPaddingValues = windowInsets
        .only(WindowInsetsSides.End)
        .asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    moodPage?.getOrNull()?.let { moodResult ->
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.surface)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Header(title = mood.name)
                }
            }

            moodResult.items.forEach { item ->
                item {
                    BasicText(
                        text = item.title,
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )
                }
                item {
                    LazyRow {
                        items(
                            items = item.items,
                            key = { it.key }
                        ) { childItem ->
                            if (childItem.key == DEFAULT_BROWSE_ID) return@items

                            when (childItem) {
                                is Innertube.AlbumItem -> AlbumItem(
                                    album = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.browseId?.let {
                                            albumRoute.global(it)
                                        }
                                    }
                                )

                                is Innertube.ArtistItem -> ArtistItem(
                                    artist = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.browseId?.let {
                                            artistRoute.global(it)
                                        }
                                    }
                                )

                                is Innertube.PlaylistItem -> PlaylistItem(
                                    playlist = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.let { endpoint ->
                                            playlistRoute.global(
                                                p0 = endpoint.browseId ?: return@clickable,
                                                p1 = endpoint.params,
                                                p2 = childItem.songCount?.let { it / 100 },
                                                p3 = true
                                            )
                                        }
                                    }
                                )

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    } ?: moodPage?.exceptionOrNull()?.let {
        BasicText(
            text = stringResource(R.string.error_message),
            style = typography.s.secondary.center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 16.dp)
        )
    } ?: ShimmerHost {
        HeaderPlaceholder(modifier = Modifier.shimmer())
        repeat(4) {
            TextPlaceholder(modifier = sectionTextModifier)
            Row {
                repeat(6) {
                    AlbumItemPlaceholder(
                        thumbnailSize = Dimensions.thumbnails.album,
                        alternative = true
                    )
                }
            }
        }
    }
}

package app.banafsh.android.ui.screens.artist

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.banafsh.android.LocalPlayerServiceBinder
import app.banafsh.android.R
import app.banafsh.android.data.models.Artist
import app.banafsh.android.db.Database
import app.banafsh.android.db.query
import app.banafsh.android.persist.PersistMapCleanup
import app.banafsh.android.persist.persist
import app.banafsh.android.preferences.UIStatePreferences
import app.banafsh.android.preferences.UIStatePreferences.artistScreenTabIndexProperty
import app.banafsh.android.providers.innertube.Innertube
import app.banafsh.android.providers.innertube.models.bodies.BrowseBody
import app.banafsh.android.providers.innertube.models.bodies.ContinuationBody
import app.banafsh.android.providers.innertube.requests.artistPage
import app.banafsh.android.providers.innertube.requests.itemsPage
import app.banafsh.android.providers.innertube.utils.from
import app.banafsh.android.ui.components.LocalMenuState
import app.banafsh.android.ui.components.themed.Header
import app.banafsh.android.ui.components.themed.HeaderIconButton
import app.banafsh.android.ui.components.themed.HeaderPlaceholder
import app.banafsh.android.ui.components.themed.NonQueuedMediaItemMenu
import app.banafsh.android.ui.components.themed.Scaffold
import app.banafsh.android.ui.components.themed.adaptiveThumbnailContent
import app.banafsh.android.ui.items.AlbumItem
import app.banafsh.android.ui.items.AlbumItemPlaceholder
import app.banafsh.android.ui.items.SongItem
import app.banafsh.android.ui.items.SongItemPlaceholder
import app.banafsh.android.ui.routing.RouteHandler
import app.banafsh.android.ui.screens.GlobalRoutes
import app.banafsh.android.ui.screens.Route
import app.banafsh.android.ui.screens.albumRoute
import app.banafsh.android.ui.screens.searchresult.ItemsPage
import app.banafsh.android.ui.theme.Dimensions
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.utils.asMediaItem
import app.banafsh.android.utils.forcePlay
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun ArtistScreen(browseId: String) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "artist/$browseId/")

    var artist by persist<Artist?>("artist/$browseId/artist")

    var artistPage by persist<Innertube.ArtistPage?>("artist/$browseId/artistPage")

    LaunchedEffect(Unit) {
        Database
            .artist(browseId)
            .combine(
                flow = artistScreenTabIndexProperty.stateFlow.map { it != 4 },
                transform = ::Pair
            )
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch))
                    withContext(Dispatchers.IO) {
                        Innertube.artistPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.name,
                                        thumbnailUrl = currentArtistPage.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt
                                    )
                                )
                            }
                    }
            }
    }

    RouteHandler(listenToGlobalEmitter = true) {
        GlobalRoutes()

        NavHost {
            val thumbnailContent = adaptiveThumbnailContent(
                isLoading = artist?.timestamp == null,
                url = artist?.thumbnailUrl,
                shape = CircleShape
            )

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (artist?.timestamp == null) HeaderPlaceholder(modifier = Modifier.shimmer()) else {
                        val (colorPalette) = LocalAppearance.current
                        val context = LocalContext.current

                        Header(title = artist?.name ?: stringResource(R.string.unknown)) {
                            textButton?.invoke()

                            Spacer(modifier = Modifier.weight(1f))

                            HeaderIconButton(
                                icon = if (artist?.bookmarkedAt == null) R.drawable.bookmark_outline
                                else R.drawable.bookmark,
                                color = colorPalette.accent,
                                onClick = {
                                    val bookmarkedAt =
                                        if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        artist
                                            ?.copy(bookmarkedAt = bookmarkedAt)
                                            ?.let(Database::update)
                                    }
                                }
                            )

                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://music.youtube.com/channel/$browseId"
                                        )
                                    }

                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                            )
                        }
                    }
                }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = UIStatePreferences.artistScreenTabIndex,
                onTabChange = { UIStatePreferences.artistScreenTabIndex = it },
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.overview), R.drawable.sparkles)
                    item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    item(2, stringResource(R.string.albums), R.drawable.disc)
                    item(3, stringResource(R.string.singles), R.drawable.disc)
                    item(4, stringResource(R.string.library), R.drawable.library)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> ArtistOverview(
                            youtubeArtistPage = artistPage,
                            thumbnailContent = thumbnailContent,
                            headerContent = headerContent,
                            onAlbumClick = { albumRoute(it) },
                            onViewAllSongsClick = { UIStatePreferences.artistScreenTabIndex = 1 },
                            onViewAllAlbumsClick = { UIStatePreferences.artistScreenTabIndex = 2 },
                            onViewAllSinglesClick = { UIStatePreferences.artistScreenTabIndex = 3 }
                        )

                        1 -> ItemsPage(
                            tag = "artist/$browseId/songs",
                            header = headerContent,
                            provider = artistPage?.let {
                                @Suppress("ktlint:standard:curly-spacing")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicResponsiveListItemRenderer = Innertube.SongItem::from
                                        )
                                    } ?: artistPage
                                        ?.songsEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicResponsiveListItemRenderer = Innertube.SongItem::from
                                            )
                                        }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.songs,
                                                continuation = null
                                            )
                                        )
                                }
                            },
                            itemContent = { song ->
                                SongItem(
                                    song = song,
                                    thumbnailSize = Dimensions.thumbnails.song,
                                    modifier = Modifier.combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem
                                                )
                                            }
                                        },
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info?.endpoint)
                                        }
                                    )
                                )
                            },
                            itemPlaceholderContent = {
                                SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
                            }
                        )

                        2 -> ItemsPage(
                            tag = "artist/$browseId/albums",
                            header = headerContent,
                            emptyItemsText = stringResource(R.string.artist_has_no_albums),
                            provider = artistPage?.let {
                                @Suppress("ktlint:standard:curly-spacing")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                        )
                                    } ?: artistPage
                                        ?.albumsEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                            )
                                        }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.albums,
                                                continuation = null
                                            )
                                        )
                                }
                            },
                            itemContent = { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    modifier = Modifier.clickable(onClick = { albumRoute(album.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                            }
                        )

                        3 -> ItemsPage(
                            tag = "artist/$browseId/singles",
                            header = headerContent,
                            emptyItemsText = stringResource(R.string.artist_has_no_singles),
                            provider = artistPage?.let {
                                @Suppress("ktlint:standard:curly-spacing")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                        )
                                    } ?: artistPage
                                        ?.singlesEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                            )
                                        }
                                        ?: Result.success(
                                            Innertube.ItemsPage(
                                                items = artistPage?.singles,
                                                continuation = null
                                            )
                                        )
                                }
                            },
                            itemContent = { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    modifier = Modifier.clickable(onClick = { albumRoute(album.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                            }
                        )

                        4 -> ArtistLocalSongs(
                            browseId = browseId,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent
                        )
                    }
                }
            }
        }
    }
}

package app.banafsh.android.data.provider

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.ALBUM
import android.provider.MediaStore.Audio.Media.ALBUM_ARTIST
import android.provider.MediaStore.Audio.Media.ALBUM_ID
import android.provider.MediaStore.Audio.Media.ARTIST
import android.provider.MediaStore.Audio.Media.ARTIST_ID
import android.provider.MediaStore.Audio.Media.DATA
import android.provider.MediaStore.Audio.Media.DATE_MODIFIED
import android.provider.MediaStore.Audio.Media.DURATION
import android.provider.MediaStore.Audio.Media.IS_MUSIC
import android.provider.MediaStore.Audio.Media.TITLE
import android.provider.MediaStore.Audio.Media.TRACK
import android.provider.MediaStore.Audio.Media.YEAR
import android.provider.MediaStore.Audio.Media._ID
import app.banafsh.android.data.model.Album
import app.banafsh.android.data.model.Artist
import app.banafsh.android.data.model.Song
import app.banafsh.android.data.model.SongAlbumMap
import app.banafsh.android.data.model.SongArtistMap
import app.banafsh.android.db.Database
import app.banafsh.android.db.transaction
import app.banafsh.android.util.isAtLeastAndroid10
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

class AudioMediaCursor(cursor: Cursor) : CursorDao(cursor) {
    companion object : CursorDaoCompanion<AudioMediaCursor>() {
        val ALBUM_URI_BASE: Uri = Uri.parse("content://media/external/audio/albumart")

        override fun order(order: SortOrder) = "$TITLE ${order.sql}"

        override fun new(cursor: Cursor) = AudioMediaCursor(cursor)

        override val uri by lazy {
            if (isAtLeastAndroid10) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        }

        override val projection by lazy {
            AudioMediaCursor(NoOpCursor).projection
        }
    }

    val isMusic by boolean(IS_MUSIC)
    val id by long(_ID)
    val title by string(TITLE)
    val duration by int(DURATION)
    val dateModified by long(DATE_MODIFIED)
    val artistId by long(ARTIST_ID)
    val artist by string(ARTIST)
    val albumId by long(ALBUM_ID)
    val album by string(ALBUM)
    val albumArtist by nullableString(ALBUM_ARTIST)
    val track by nullableInt(TRACK)
    val year by nullableString(YEAR)
    val path by string(DATA)

    val albumUri get() = ContentUris.withAppendedId(ALBUM_URI_BASE, albumId)
}

private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))

fun Context.musicFilesAsFlow(): StateFlow<List<Triple<Song, Pair<Artist, SongArtistMap>, Pair<Album, SongAlbumMap>>>> =
    flow {
        var version: String? = null

        while (currentCoroutineContext().isActive) {
            val newVersion = MediaStore.getVersion(applicationContext)

            if (version != newVersion) {
                version = newVersion

                AudioMediaCursor.query(contentResolver) {
                    buildList {
                        while (next()) {
                            if (!isMusic || duration == 0) continue
                            add(
                                Triple(
                                    Song(
                                        id = id.toString(),
                                        title = title,
                                        artist = artist,
                                        duration = duration,
                                        dateModified = dateModified,
                                        thumbnailUrl = albumUri.toString(),
                                        path = path,
                                    ),
                                    Pair(
                                        Artist(
                                            id = artistId.toString(),
                                            name = artist,
                                            thumbnailUrl = albumUri.toString(),
                                        ),
                                        SongArtistMap(
                                            songId = id.toString(),
                                            artistId = artistId.toString(),
                                        ),
                                    ),
                                    Pair(
                                        Album(
                                            id = albumId.toString(),
                                            title = album,
                                            authors = albumArtist,
                                            year = year,
                                            thumbnailUrl = albumUri.toString(),
                                        ),
                                        SongAlbumMap(
                                            songId = id.toString(),
                                            albumId = albumId.toString(),
                                            position = track,
                                        ),
                                    ),
                                ),
                            )
                        }
                    }
                }?.let { emit(it) }
            }
            delay(5.seconds)
        }
    }.distinctUntilChanged()
        .onEach { songs ->
            transaction { songs.forEach(Database::insert) }
        }
        .stateIn(mediaScope, SharingStarted.Eagerly, listOf())

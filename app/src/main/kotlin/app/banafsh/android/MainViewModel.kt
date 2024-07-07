package app.banafsh.android

import android.Manifest
import android.content.Context
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.banafsh.android.lib.core.ui.utils.isAtLeastAndroid13
import app.banafsh.android.models.Album
import app.banafsh.android.models.Artist
import app.banafsh.android.models.Song
import app.banafsh.android.models.SongAlbumMap
import app.banafsh.android.models.SongArtistMap
import app.banafsh.android.service.LOCAL_KEY_PREFIX
import app.banafsh.android.service.PlayerService
import app.banafsh.android.utils.AudioMediaCursor
import app.banafsh.android.utils.hasPermissions
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val readPermission = if (isAtLeastAndroid13) Manifest.permission.READ_MEDIA_AUDIO
else Manifest.permission.READ_EXTERNAL_STORAGE

val permissions = arrayOf(readPermission, Manifest.permission.WRITE_EXTERNAL_STORAGE)

class MainViewModel : ViewModel() {
    var binder: PlayerService.Binder? by mutableStateOf(null)

    private val _hasPermissions =
        MutableStateFlow(Dependencies.application.applicationContext.hasPermissions(permissions))
    val hasPermissions: StateFlow<Boolean> = _hasPermissions

    private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))

    init {
        viewModelScope.launch {
            musicFilesAsFlow(Dependencies.application.applicationContext).collect {}
        }
    }

    fun setHasPermissions(hasPermissions: Boolean) {
        _hasPermissions.value = hasPermissions
    }

    private fun musicFilesAsFlow(context: Context):
        StateFlow<List<Triple<Song, Pair<Album, SongAlbumMap>, Pair<Artist, SongArtistMap>>>> = flow {
        var version: String? = null

        while (currentCoroutineContext().isActive) {
            val newVersion = MediaStore.getVersion(context.applicationContext)

            if (version != newVersion) {
                version = newVersion

                AudioMediaCursor.query(context.contentResolver) {
                    buildList {
                        while (next()) {
                            if (!isMusic || duration == 0) continue
                            add(
                                Triple(
                                    Song(
                                        id = "$LOCAL_KEY_PREFIX$id",
                                        title = title,
                                        artistsText = artist,
                                        durationText = duration.milliseconds.toComponents { minutes, seconds, _ ->
                                            "$minutes:${seconds.toString().padStart(2, '0')}"
                                        },
                                        dateModified = dateModified,
                                        thumbnailUrl = albumUri.toString(),
                                        path = path
                                    ),
                                    Pair(
                                        Album(
                                            id = albumId.toString(),
                                            title = album,
                                            thumbnailUrl = albumUri.toString(),
                                            artistsText = albumArtist
                                        ),
                                        SongAlbumMap(
                                            songId = "$LOCAL_KEY_PREFIX$id",
                                            albumId = albumId.toString(),
                                            position = position
                                        )
                                    ),
                                    Pair(
                                        Artist(
                                            id = artistId.toString(),
                                            name = artist,
                                            thumbnailUrl = albumUri.toString()
                                        ),
                                        SongArtistMap(
                                            songId = "$LOCAL_KEY_PREFIX$id",
                                            artistId = artistId.toString()
                                        )
                                    )
                                )
                            )
                        }
                    }
                }?.let { emit(it) }
            }
            delay(5.seconds)
        }
    }.distinctUntilChanged()
        .onEach { songs ->
            transactionTemp { songs.forEach(TempDatabase::insert) }
            transaction { songs.forEach(Database::insert) }
        }
        .stateIn(mediaScope, SharingStarted.Eagerly, listOf())
}

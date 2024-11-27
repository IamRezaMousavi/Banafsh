package app.banafsh.android.util

import android.content.ContentUris
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.banafsh.android.data.model.Song

fun Song.getUri() = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    id.toLong(),
)

val Song.asMediaItem: MediaItem
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "duration" to duration,
                    ),
                )
                .build(),
        )
        .setMediaId(id)
        .setUri(getUri())
        .build()

fun MediaItem.toSong(): Song = Song(
    id = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString(),
    duration = mediaMetadata.extras?.getInt("duration"),
    thumbnailUrl = mediaMetadata.artworkUri?.toString(),
)

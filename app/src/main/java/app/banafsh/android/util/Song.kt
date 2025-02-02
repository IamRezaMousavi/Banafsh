package app.banafsh.android.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.banafsh.android.data.model.Song
import java.io.File

fun Song.getUri() = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    id.toLong(),
)

fun MediaItem.getUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    mediaId.toLong(),
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
    path = getUri().path,
)

fun Song.shareSongIndent(): Intent = Intent().apply {
    action = Intent.ACTION_SEND
    type = "audio/*"
    putExtra(
        Intent.EXTRA_STREAM,
        getUri(),
    )
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun Song.delete(context: Context): Boolean {
    if (!path.isNullOrBlank()) {
        val f = File(path)
        if (f.delete()) {
            context.contentResolver.delete(getUri(), null, null)
            return true
        }
    }
    return false
}

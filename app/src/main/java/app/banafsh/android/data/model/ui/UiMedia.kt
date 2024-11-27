package app.banafsh.android.data.model.ui

import androidx.media3.common.MediaItem

data class UiMedia(val id: String, val title: String, val artist: String, val duration: Long)

fun MediaItem.toUiMedia(duration: Long) = UiMedia(
    id = mediaId,
    title = mediaMetadata.title?.toString().orEmpty(),
    artist = mediaMetadata.artist?.toString().orEmpty(),
    duration = duration,
)

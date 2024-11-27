package app.banafsh.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String? = null,
    val duration: Int? = null,
    val dateModified: Long? = null,
    val likedAt: Long? = null,
    val totalPlayTimeMs: Long? = null,
    val thumbnailUrl: String? = null,
    val path: String? = null,
) {
    fun toggleLike() = copy(likedAt = if (likedAt == null) System.currentTimeMillis() else null)
}

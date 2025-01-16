package app.banafsh.android.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Album(
    @PrimaryKey val id: String,
    val title: String? = null,
    val description: String? = null,
    val authors: String? = null,
    val year: String? = null,
    val thumbnailUrl: String? = null,
    val shareUrl: String? = null,
    val timestamp: Long? = null,
    val bookmarkedAt: Long? = null,
    val otherInfo: String? = null,
)

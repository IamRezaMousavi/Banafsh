package app.banafsh.android.model

data class Song(
    val id: String,
    val title: String,
    val artist: String? = null,
    val duration: Long? = null,
    val dateModified: Long? = null,
)

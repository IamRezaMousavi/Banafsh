package app.banafsh.android.data.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.banafsh.android.providers.piped.models.authenticatedWith
import io.ktor.http.Url

@Immutable
@Entity(
    indices = [
        Index(
            value = ["apiBaseUrl", "username"],
            unique = true
        )
    ]
)
data class PipedSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apiBaseUrl: Url,
    val token: String,
    val username: String // the username should never change on piped
) {
    fun toApiSession() = apiBaseUrl authenticatedWith token
}
package app.banafsh.android.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
class QueuedSong(@PrimaryKey(autoGenerate = true) val itemId: Long = 0, val songId: String, var position: Long?)

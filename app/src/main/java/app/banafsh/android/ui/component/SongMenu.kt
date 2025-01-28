package app.banafsh.android.ui.component

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.banafsh.android.R
import app.banafsh.android.data.model.Song
import app.banafsh.android.db.Database
import app.banafsh.android.db.query
import app.banafsh.android.ui.item.SongItem
import app.banafsh.android.util.shareSongIndent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SongMenu(song: Song, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var likedAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            launch {
                Database
                    .likedAt(song.id)
                    .collect { likedAt = it }
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(end = 12.dp),
    ) {
        SongItem(
            song = song,
            modifier = Modifier.weight(1f),
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    query {
                        if (
                            Database.like(
                                songId = song.id,
                                likedAt = if (likedAt == null) System.currentTimeMillis() else null,
                            ) != 0
                        ) return@query

                        Database.update(song.toggleLike())
                    }
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .size(18.dp),
            )

            IconButton(
                icon = R.drawable.share_social,
                color = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    val sendIntent = song.shareSongIndent()
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .size(17.dp),
            )
        }
    }
}

package app.banafsh.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.data.model.Song
import app.banafsh.android.ui.item.SongItem

@Composable
fun HomeScreen(
    progress: Float,
    onProgress: (Float) -> Unit,
    currentPlayingSong: Song,
    isAudioPlaying: Boolean,
    songList: List<Song>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    SongItem(currentPlayingSong)
                    MediaPlayerController(isAudioPlaying, onStart, onNext)
                    Spacer(modifier = Modifier.size(8.dp))
                    Slider(
                        value = progress,
                        onValueChange = onProgress,
                        valueRange = 0f..100f,
                    )
                }
            }
        },
    ) {
        LazyColumn(
            contentPadding = it,
        ) {
            itemsIndexed(songList) { index, item ->
                SongItem(item, modifier = Modifier.clickable { onItemClick(index) })
            }
        }
    }
}

@Composable
fun MediaPlayerController(isAudioPlaying: Boolean, onStart: () -> Unit, onNext: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp),
    ) {
        IconButton(
            onClick = onStart,
        ) {
            Icon(
                painter = painterResource(if (isAudioPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(
            onClick = onNext,
        ) {
            Icon(painter = painterResource(R.drawable.play_skip_forward), contentDescription = null)
        }
    }
}

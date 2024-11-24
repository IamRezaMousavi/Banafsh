package app.banafsh.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Song
import app.banafsh.android.db.Database
import app.banafsh.android.service.PEvent
import app.banafsh.android.service.PState
import app.banafsh.android.service.PlayerServiceHandler
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val playerServiceHandler: PlayerServiceHandler) : ViewModel() {
    var isPlaying by mutableStateOf(false)
    var progress by mutableFloatStateOf(0f)
    var progressString by mutableStateOf("00:00")
    var duration by mutableLongStateOf(0L)
    var currentSelectedAudio by mutableStateOf(Song(id = "", title = "None"))
    var audioList by mutableStateOf(emptyList<Song>())

    private val _uistate: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uistate = _uistate.asStateFlow()

    init {
        loadSongData()
    }

    init {
        viewModelScope.launch {
            playerServiceHandler.pState.collectLatest { mediaState ->
                when (mediaState) {
                    PState.Initial -> _uistate.value = UIState.Initial
                    is PState.Buffering -> calculateProgressValue(mediaState.progress)
                    is PState.Playing -> isPlaying = mediaState.isPlaying
                    is PState.Progress -> calculateProgressValue(mediaState.progress)
                    is PState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }
                    is PState.Ready -> {
                        duration = mediaState.duration
                        _uistate.value = UIState.Ready
                    }
                }
            }
        }
    }

    private fun loadSongData() {
        viewModelScope.launch {
            Database.songs(SongSortBy.Title, SortOrder.Ascending).collectLatest { songs ->
                audioList = songs
                setMediaItems()
            }
        }
    }

    private fun setMediaItems() {
        audioList.map { song ->
            MediaItem.Builder()
                .setUri(song.path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(song.artist)
                        .setDisplayTitle(song.title)
                        .setSubtitle(song.artist)
                        .build(),
                )
                .build()
        }.also {
            playerServiceHandler.setMediaItemList(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress = if (currentProgress > 0) (currentProgress.toFloat() / duration.toFloat() * 100f) else 0f
        progressString = formatDuration(currentProgress)
    }

    fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val second = minute - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, second)
    }

    fun onUIEvent(uiEvent: UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            UIEvent.Backward -> playerServiceHandler.onPlayerEvent(PEvent.Backward)
            UIEvent.Forward -> playerServiceHandler.onPlayerEvent(PEvent.Forward)
            UIEvent.SeekToNext -> playerServiceHandler.onPlayerEvent(PEvent.SeekToNext)
            is UIEvent.PlayPause -> playerServiceHandler.onPlayerEvent(PEvent.PlayPause)
            is UIEvent.SeekTo -> playerServiceHandler.onPlayerEvent(
                PEvent.SeekTo,
                seekPosition = (duration * uiEvent.position / 100f).toLong(),
            )
            is UIEvent.SelectedAudioChange -> {
                playerServiceHandler.onPlayerEvent(
                    PEvent.SelectAudioChange,
                    selectedItemIndex = uiEvent.index,
                )
            }
            is UIEvent.UpdateProgress -> {
                playerServiceHandler.onPlayerEvent(
                    PEvent.UpdateProgress(uiEvent.newProgress),
                )
                progress = uiEvent.newProgress
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            playerServiceHandler.onPlayerEvent(PEvent.Stop)
        }
        super.onCleared()
    }
}

sealed class UIEvent {
    object PlayPause : UIEvent()
    data class SelectedAudioChange(val index: Int) : UIEvent()
    data class SeekTo(val position: Float) : UIEvent()
    object SeekToNext : UIEvent()
    object Backward : UIEvent()
    object Forward : UIEvent()
    data class UpdateProgress(val newProgress: Float) : UIEvent()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}

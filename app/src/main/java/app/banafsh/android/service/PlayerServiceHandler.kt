package app.banafsh.android.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerServiceHandler(private val exoPlayer: ExoPlayer) : Player.Listener {

    private val _pState: MutableStateFlow<PState> = MutableStateFlow(PState.Initial)
    val pState = _pState.asStateFlow()

    private val job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvent(playerEvent: PEvent, selectedItemIndex: Int = -1, seekPosition: Long = 0) {
        when (playerEvent) {
            PEvent.Backward -> exoPlayer.seekBack()
            PEvent.Forward -> exoPlayer.seekForward()
            PEvent.SeekToNext -> exoPlayer.seekToNext()
            PEvent.PlayPause -> playOrPause()
            PEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            PEvent.SelectAudioChange -> {
                when (selectedItemIndex) {
                    exoPlayer.currentMediaItemIndex -> playOrPause()
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedItemIndex)
                        _pState.value = PState.Playing(isPlaying = true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            PEvent.Stop -> stopProgressUpdate()
            is PEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong(),
                )
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING ->
                _pState.value =
                    PState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY ->
                _pState.value =
                    PState.Ready(exoPlayer.duration)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _pState.value = PState.Playing(isPlaying)
        _pState.value = PState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
        }
    }

    private suspend fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _pState.value = PState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(500)
            _pState.value = PState.Progress(exoPlayer.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _pState.value = PState.Playing(isPlaying = false)
    }
}

sealed class PEvent {
    object PlayPause : PEvent()
    object SelectAudioChange : PEvent()
    object Backward : PEvent()
    object SeekToNext : PEvent()
    object Forward : PEvent()
    object SeekTo : PEvent()
    object Stop : PEvent()
    data class UpdateProgress(val newProgress: Float) : PEvent()
}

sealed class PState {
    object Initial : PState()
    data class Ready(val duration: Long) : PState()
    data class Progress(val progress: Long) : PState()
    data class Buffering(val progress: Long) : PState()
    data class Playing(val isPlaying: Boolean) : PState()
    data class CurrentPlaying(val mediaItemIndex: Int) : PState()
}

package app.banafsh.android.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.banafsh.android.util.isAtLeastAndroid8

class PlayerService : MediaSessionService() {
    lateinit var mediaSession: MediaSession

    lateinit var notificationManager: PNotificationManager

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setTrackSelector(DefaultTrackSelector(this))
            .build()
        mediaSession = MediaSession.Builder(this, player).build()

        notificationManager = PNotificationManager(this, player)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isAtLeastAndroid8) {
            notificationManager.startNotificationService(
                mediaSession = mediaSession,
                mediaSessionService = this,
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.playWhenReady = false
                player.stop()
            }
        }
        super.onDestroy()
    }
}

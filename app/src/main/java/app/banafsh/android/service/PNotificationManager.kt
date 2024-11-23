package app.banafsh.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.banafsh.android.R
import app.banafsh.android.util.isAtLeastAndroid8
import coil.imageLoader
import coil.request.ImageRequest

private const val NOTIFICATION_ID = 101
private const val NOTIFICATION_CHANNEL_NAME = "player channel"
private const val NOTIFICATION_CHANNEL_ID = "player channel"

class PNotificationManager(private val context: Context, private val exoPlayer: ExoPlayer) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        if (isAtLeastAndroid8) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startNotificationService(mediaSessionService: MediaSessionService, mediaSession: MediaSession) {
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startForegroundNotificationService(mediaSessionService: MediaSessionService) {
        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(mediaSession: MediaSession) {
        PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
        )
            .setMediaDescriptionAdapter(
                PlayerNotificationAdapter(
                    context = context,
                    pendingIntent = mediaSession.sessionActivity,
                ),
            )
            .setSmallIconResourceId(R.drawable.app_icon)
            .build()
            .also {
                it.setMediaSessionToken(mediaSession.platformToken)
                it.setUseFastForwardActionInCompactView(true)
                it.setUseRewindActionInCompactView(true)
                it.setUseNextActionInCompactView(true)
                it.setPriority(NotificationCompat.PRIORITY_LOW)
                it.setPlayer(exoPlayer)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)
    }
}

@UnstableApi
class PlayerNotificationAdapter(private val context: Context, private val pendingIntent: PendingIntent?) :
    PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence = player.mediaMetadata.albumTitle ?: "Unknown"

    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    override fun getCurrentContentText(player: Player): CharSequence = player.mediaMetadata.displayTitle ?: "Unknown"

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(player.mediaMetadata.artworkUri)
            .target { result ->
                val bitmap = (result as BitmapDrawable).bitmap
                callback.onBitmap(bitmap)
            }
            .build()
        context.imageLoader.enqueue(request)
        return null
    }
}

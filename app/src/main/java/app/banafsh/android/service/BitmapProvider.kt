package app.banafsh.android.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.graphics.applyCanvas
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest

class BitmapProvider(
    private val context: Context,
    private val getBitmapSize: () -> Int,
    private val getColor: (isDark: Boolean) -> Int,
) {
    var lastUri: Uri? = null
        private set

    private var lastBitmap: Bitmap? = null
        set(value) {
            field = value
            listener?.invoke(value)
        }
    private var lastIsSystemInDarkMode = false
    private var currentTask: Disposable? = null

    private lateinit var defaultBitmap: Bitmap
    val bitmap get() = lastBitmap ?: defaultBitmap

    private var listener: ((Bitmap?) -> Unit)? = null

    init {
        setDefaultBitmap()
    }

    fun setDefaultBitmap(): Boolean {
        val isSystemInDarkMode = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (::defaultBitmap.isInitialized && isSystemInDarkMode == lastIsSystemInDarkMode) return false

        lastIsSystemInDarkMode = isSystemInDarkMode

        val size = getBitmapSize()
        defaultBitmap = Bitmap.createBitmap(
            /* width = */ size,
            /* height = */ size,
            /* config = */ Bitmap.Config.ARGB_8888,
        ).applyCanvas {
            drawColor(getColor(isSystemInDarkMode))
        }

        return lastBitmap == null
    }

    fun load(uri: Uri?, onDone: (Bitmap) -> Unit = {}) {
        if (lastUri == uri) {
            listener?.invoke(lastBitmap)
            return
        }

        currentTask?.dispose()
        lastUri = uri

        if (uri == null) {
            lastBitmap = null
            onDone(bitmap)
            return
        }

        currentTask = context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .listener(
                    onError = { _, _ ->
                        lastBitmap = null
                        onDone(bitmap)
                    },
                    onSuccess = { _, result ->
                        lastBitmap = (result.drawable as BitmapDrawable).bitmap
                        onDone(bitmap)
                    },
                )
                .build(),
        )
    }

    fun setListener(callback: ((Bitmap?) -> Unit)?) {
        listener = callback
        listener?.invoke(lastBitmap)
    }
}

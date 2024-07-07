package app.banafsh.android

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.work.Configuration
import app.banafsh.android.lib.compose.persist.PersistMap
import app.banafsh.android.lib.compose.preferences.PreferencesHolder
import app.banafsh.android.preferences.DataPreferences
import app.banafsh.android.utils.intent
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import com.kieronquinn.monetcompat.core.MonetCompat
import kotlin.system.exitProcess

class MainApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    override fun onCreate() {
        // setup bug handler activity
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            val exceptionMessage = Log.getStackTraceString(error)
            val threadName = thread.name
            val intent = Intent(this, BugReporterActivity::class.java).apply {
                putExtra("exception_message", exceptionMessage)
                putExtra("thread_name", threadName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            exitProcess(10)
        }

        MonetCompat.debugLog = BuildConfig.DEBUG
        super.onCreate()

        Dependencies.init(this)
        MonetCompat.enablePaletteCompat()
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes(DataPreferences.coilDiskCacheMaxSize.bytes)
                .build()
        )
        .let { if (BuildConfig.DEBUG) it.logger(DebugLogger()) else it }
        .build()

    val persistMap = PersistMap()

    override val workManagerConfiguration = Configuration.Builder()
        .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
        .build()
}

object Dependencies {
    lateinit var application: MainApplication
        private set

    val credentialManager by lazy { CredentialManager.create(application) }

    internal fun init(application: MainApplication) {
        this.application = application
        TempDatabaseInitializer()
        DatabaseInitializer()
    }
}

open class GlobalPreferencesHolder : PreferencesHolder(Dependencies.application, "preferences")

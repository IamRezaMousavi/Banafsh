package app.banafsh.android

import android.app.Application
import app.banafsh.android.preference.PreferencesHolder

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Dependencies.init(this)
    }
}

object Dependencies {
    lateinit var application: MainApplication
        private set

    internal fun init(application: MainApplication) {
        this.application = application
    }
}

open class GlobalPreferencesHolder : PreferencesHolder(Dependencies.application, "preferences")

package com.tingjizhushou

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for TingJiZhuShou app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class TingJiApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        /**
         * Singleton instance of the application.
         * Used for accessing application context from non-Activity classes.
         */
        lateinit var instance: TingJiApplication
            private set
    }
}

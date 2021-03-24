package io.helpdesk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.helpdesk.BuildConfig.DEBUG
import timber.log.Timber

/**
 * application entry point
 */
@HiltAndroidApp
class HelpDeskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
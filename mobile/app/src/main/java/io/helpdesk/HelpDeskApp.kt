package io.helpdesk

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
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
        if (DEBUG) Timber.plant(Timber.DebugTree())

        // initialize firebase
        Firebase.initialize(this).apply {
            Timber.tag("HelpDesk Firebase").d("Firebase SDKs added -> ${this?.name}")
        }
    }
}
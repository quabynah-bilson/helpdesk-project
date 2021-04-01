package io.helpdesk

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.HiltAndroidApp
import io.helpdesk.BuildConfig.DEBUG
import timber.log.Timber
import javax.inject.Inject

/**
 * application entry point
 */
@HiltAndroidApp
class HelpDeskApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Timber.plant(Timber.DebugTree())

        // initialize firebase
        Firebase.initialize(this).apply {
            Timber.tag("HelpDesk Firebase").d("Firebase SDKs added -> ${this?.name}")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.INFO)
        .setWorkerFactory(workerFactory)
        .build()
}
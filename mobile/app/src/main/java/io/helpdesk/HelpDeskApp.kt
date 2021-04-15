package io.helpdesk

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import io.helpdesk.BuildConfig.DEBUG
import io.helpdesk.core.util.NotificationUtil
import io.helpdesk.core.util.logger
import timber.log.Timber
import javax.inject.Inject

/**
 * application entry point
 */
@HiltAndroidApp
class HelpDeskApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var messaging: FirebaseMessaging

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Timber.plant(Timber.DebugTree())

        // initialize firebase
        Firebase.initialize(this).apply {
            logger.d("Firebase SDKs added -> ${this?.name}")

            // subscribe to topic
            messaging.subscribeToTopic(getString(R.string.app_name))
        }

        // create notification channels
        NotificationUtil.createChannels(applicationContext)

        // fixme -> remove this line
        NotificationUtil.push(
            this,
            title = "Hello world",
            message = "This is so cool",
            payload = "helpdesk payload",
        )
    }

    override fun onTerminate() {
        messaging.unsubscribeFromTopic(getString(R.string.app_name))
        super.onTerminate()
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.INFO)
        .setWorkerFactory(workerFactory)
        .build()
}
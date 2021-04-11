package io.helpdesk.core.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.helpdesk.MainActivity
import io.helpdesk.R
import kotlin.random.Random


object NotificationUtil {
    const val FEEDBACK_CHANNEL = "channel.feedback"
    const val TICKET_CHANNEL = "channel.ticket"


    /**
     * push notification
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    fun push(
        context: Context,
        title: String,
        message: String,
        channelId: String = FEEDBACK_CHANNEL,
        payload: String?,
        type: NotificationType = NotificationType.Feedback,
    ) {
        val icon =
            if (type == NotificationType.Feedback) R.drawable.ic_send else R.drawable.ic_account

        //create an intent to open the main activity
        val intent = Intent(
            context,
            MainActivity::class.java
        )

        //put together the PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            FLAG_UPDATE_CURRENT
        )

        // build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(context.resources.getColor(R.color.blue_200, null))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // trigger the notification
        val manager = NotificationManagerCompat.from(context)

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates
        manager.notify(Random.nextInt(10), notificationBuilder.build())
    }

    /**
     * create a notification channel
     */
    fun createChannels(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            //build the actual notification channel, giving it a unique ID and name
            val ticketChannel =
                NotificationChannel(
                    TICKET_CHANNEL,
                    TICKET_CHANNEL,
                    importance
                )

            with(ticketChannel) {
                //we can optionally add a description for the channel
                val channelDescription =
                    "A channel which shows notifications about tickets"
                description = channelDescription
                //we can optionally set notification LED colour
                lightColor = Color.MAGENTA
                vibrationPattern = longArrayOf(200, 300, 100, 300, 200)

                // Register the channel with the system
                (context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager?)?.createNotificationChannel(this)
            }

            //build the actual notification channel, giving it a unique ID and name
            val feedbackChannel =
                NotificationChannel(
                    FEEDBACK_CHANNEL,
                    FEEDBACK_CHANNEL,
                    importance
                )

            with(feedbackChannel) {
                //we can optionally add a description for the channel
                val feedbackDescription =
                    "A channel which shows notifications about tickets feedback"
                description = feedbackDescription
                //we can optionally set notification LED colour
                lightColor = Color.YELLOW
                vibrationPattern = longArrayOf(200, 300, 100, 300, 200)

                // Register the channel with the system
                (context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager?)?.createNotificationChannel(this)
            }
        }
    }
}

enum class NotificationType { Feedback, Ticket }
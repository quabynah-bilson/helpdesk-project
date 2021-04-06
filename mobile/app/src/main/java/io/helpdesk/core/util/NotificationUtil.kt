package io.helpdesk.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.helpdesk.R

object NotificationUtil {
    private const val FEEDBACK_CHANNEL = "channel.feedback"
    private const val TICKET_CHANNEL = "channel.ticket"

    fun push(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        payload: Parcelable?,
//        target: KClass<Fragment>,
        type: NotificationType = NotificationType.Feedback,
    ) {
        val icon =
            if (type == NotificationType.Feedback) R.drawable.ic_send else R.drawable.ic_account

        // build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
//            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // trigger the notification
        val manager = NotificationManagerCompat.from(context)

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates
        manager.notify(0, notificationBuilder.build())
    }

    fun createChannel(context: Context) {
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
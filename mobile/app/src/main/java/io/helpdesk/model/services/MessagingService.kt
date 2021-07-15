package io.helpdesk.model.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.helpdesk.core.util.NotificationType
import io.helpdesk.core.util.NotificationUtil
import timber.log.Timber

/**
 * A service that extends FirebaseMessagingService.
 * This is required if you want to do any message handling beyond receiving
 * notifications on apps in the background. To receive notifications
 * in foregrounded apps, to receive data payload, to send upstream messages, and so on,
 * you must extend this service
 *
 *
 * ref: https://firebase.google.com/docs/cloud-messaging/android/client
 */
class BaseMessagingService : FirebaseMessagingService() {

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(p0: String) {
        Timber.tag("new-token").i("Refreshed token -> $p0")
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Timber.tag("on-receive-messages").i("from: ${p0.from}")
        super.onMessageReceived(p0)

        if (p0.data.isNotEmpty()) {
            Timber.tag("on-receive-messages").i("Message data payload: ${p0.data}")
            when (p0.data["type"]) {
                "tickets" -> {
                    // get id
                    val ticketId = p0.data["id"]

                    // push notification
                    NotificationUtil.push(
                        applicationContext,
                        title = "Ticket notification",
                        message = "Ticket update sent. Tap here for more details",
                        type = NotificationType.Ticket,
                        payload = ticketId,
                    )
                }
            }
        }

        // Check if message contains a notification payload.
        p0.notification?.let {
            Timber.tag("on-receive-messages").i("Message Notification Body: ${it.body}")
            NotificationUtil.push(
                applicationContext,
                title = it.title ?: "New notification received",
                message = it.body ?: "Tap here for more details",
                type = NotificationType.Feedback,
                payload = it.tag,
            )
        }
    }

    override fun onDeletedMessages() {
        Timber.tag("on-delete-messages").i("message(s) deleted from FCM")
        super.onDeletedMessages()
    }
}
package com.spgrvl.kissos.wallet.android.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.spgrvl.kissos.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationService @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    fun defaultKissosNotification(
        channel: KissosNotificationChannel,
        autoCancel: Boolean = true,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ): KissosNotification {
        val kissosNotification = KissosNotification(context, channel)
        val color = ContextCompat.getColor(context, R.color.green)
        kissosNotification.setSmallIcon(R.drawable.ic_notification)
            .setColor(color)
            .setPriority(priority)
            .setColorized(true)
            .setLights(color, 1000, 200)
            .setAutoCancel(autoCancel)
        return kissosNotification
    }

    fun showNotification(
        notification: NotificationCompat.Builder,
        notificationId: Int
    ) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return
            // Register the channel with the system
            val channel = (notification as KissosNotification).kissosChannel.create(context)

            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(notificationId, notification.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissNotification(notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}

package com.spgrvl.kissos.wallet.android.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.spgrvl.kissos.ui.R

enum class KissosNotificationChannel(
    val channelId: String,
    @StringRes val channelName: Int,
    @StringRes val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_MAX,
    val bypassDnd: Boolean = true
) {
    TRANSACTION_REMINDER(
        channelId = "transaction_reminder",
        channelName = R.string.reminder_channel_name,
        description = R.string.reminder_channel_description,
        importance = NotificationManager.IMPORTANCE_HIGH,
        bypassDnd = false
    );

    @SuppressLint("WrongConstant")
    fun create(context: Context): NotificationChannel {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val colorPurple = ContextCompat.getColor(context, R.color.green)
        val channel = NotificationChannel(
            channelId,
            context.getString(channelName),
            importance
        )
        channel.description = context.getString(description)
        channel.lightColor = colorPurple
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.setBypassDnd(false)
        return channel
    }
}

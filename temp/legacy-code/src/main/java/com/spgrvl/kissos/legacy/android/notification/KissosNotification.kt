package com.spgrvl.kissos.wallet.android.notification

import android.content.Context
import androidx.core.app.NotificationCompat

class KissosNotification(
    context: Context,
    val kissosChannel: KissosNotificationChannel
) : NotificationCompat.Builder(context, kissosChannel.channelId)

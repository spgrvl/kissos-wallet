package com.spgrvl.kissos.domain.banknotification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import java.util.UUID

/** Identifies Kissos's notification about a detected payment, so it goes away once the payment is handled. */
object DetectedPaymentNotifications {
    const val TAG = "detected_payment"

    fun id(paymentId: UUID): Int = paymentId.hashCode()

    fun cancel(context: Context, paymentId: UUID) {
        NotificationManagerCompat.from(context).cancel(TAG, id(paymentId))
    }
}

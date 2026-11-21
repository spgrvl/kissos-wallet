package com.spgrvl.kissos.banknotifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.domain.banknotification.DetectedPaymentNotifications
import com.spgrvl.kissos.domain.banknotification.PaymentDraft
import com.spgrvl.kissos.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.util.Currency
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Kissos's own notification about a payment it detected: add, change or dismiss it, or undo adding it. */
@Singleton
class DetectedPaymentNotifier @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun showDetected(draft: PaymentDraft) {
        val payment = draft.payment
        val account = draft.account
        val text = when {
            draft.possibleDuplicate -> context.getString(R.string.detected_payment_possible_duplicate)
            account == null -> context.getString(R.string.detected_payment_pick_account)
            account.asset.code != payment.currency -> context.getString(R.string.detected_payment_other_currency)
            else -> context.getString(R.string.detected_payment_add_to, account.name.value)
        }
        val edit = openIntent(payment.id)
        val builder = builder(payment)
            .setContentTitle(title(draft))
            .setContentText(text)
            .setContentIntent(edit)
        if (draft.canAddDirectly) {
            builder.addAction(0, context.getString(R.string.add), action(payment.id, ACTION_ADD))
        }
        builder
            .addAction(0, context.getString(R.string.edit), edit)
            .addAction(0, context.getString(R.string.dismiss), action(payment.id, ACTION_DISMISS))
        notify(payment.id, builder)
    }

    fun showAdded(draft: PaymentDraft) {
        val payment = draft.payment
        val builder = builder(payment)
            .setContentTitle(title(draft))
            .setContentText(context.getString(R.string.detected_payment_added_to, draft.account?.name?.value.orEmpty()))
            .setSilent(true)
            .setTimeoutAfter(ADDED_TIMEOUT_MS)
            .addAction(0, context.getString(R.string.undo), action(payment.id, ACTION_UNDO))
        notify(payment.id, builder)
    }

    fun cancel(paymentId: UUID) {
        manager.cancel(DetectedPaymentNotifications.TAG, DetectedPaymentNotifications.id(paymentId))
    }

    private fun title(draft: PaymentDraft): String =
        listOfNotNull(money(draft.payment), draft.title).joinToString(" · ")

    private fun builder(payment: DetectedPaymentEntity): NotificationCompat.Builder {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.detected_payments))
                .setDescription(context.getString(R.string.detected_payments_channel_description))
                .build()
        )
        return NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(payment.postedAt.toEpochMilli())
            .setShowWhen(true)
            .setAutoCancel(true)
    }

    private fun notify(paymentId: UUID, builder: NotificationCompat.Builder) {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (allowed) {
            val id = DetectedPaymentNotifications.id(paymentId)
            manager.notify(DetectedPaymentNotifications.TAG, id, builder.build())
        }
    }

    private fun action(paymentId: UUID, action: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, DetectedPaymentActionReceiver::class.java)
            .setAction(action)
            .setData(paymentUri(paymentId)),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Opens the expense screen filled in from the payment. */
    private fun openIntent(paymentId: UUID): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        context.packageManager.getLaunchIntentForPackage(context.packageName)!!
            .setData(paymentUri(paymentId))
            .putExtra(BankNotificationIntents.EXTRA_DETECTED_PAYMENT_ID, paymentId.toString())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Keeps each payment's intents apart, since Intent.filterEquals() ignores extras. */
    private fun paymentUri(paymentId: UUID): Uri = Uri.parse("kissos://detected-payment/$paymentId")

    companion object {
        private const val CHANNEL = "detected_payments"
        private const val ADDED_TIMEOUT_MS = 60_000L

        const val ACTION_ADD = "com.spgrvl.kissos.banknotifications.ADD"
        const val ACTION_DISMISS = "com.spgrvl.kissos.banknotifications.DISMISS"
        const val ACTION_UNDO = "com.spgrvl.kissos.banknotifications.UNDO"

        fun money(payment: DetectedPaymentEntity): String = try {
            NumberFormat.getCurrencyInstance()
                .apply { currency = Currency.getInstance(payment.currency) }
                .format(payment.amount)
        } catch (e: IllegalArgumentException) {
            "${payment.amount} ${payment.currency}"
        }
    }
}

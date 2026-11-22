package com.spgrvl.kissos.banknotifications

import com.spgrvl.kissos.banknotifications.parse.BankNotificationParsers
import com.spgrvl.kissos.banknotifications.parse.NotificationText
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.UnparsedNotificationEntity
import com.spgrvl.kissos.domain.banknotification.BankNotificationPrefs
import com.spgrvl.kissos.domain.banknotification.DetectedPayments
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/** Handles a notification from one of the user's bank apps: a payment to add or ask about, or nothing. */
class BankNotificationProcessor @Inject constructor(
    private val dao: BankNotificationDao,
    private val prefs: BankNotificationPrefs,
    private val payments: DetectedPayments,
    private val notifier: DetectedPaymentNotifier,
    private val timeProvider: TimeProvider,
) {
    suspend fun process(packageName: String, notificationKey: String, postedAt: Instant, text: NotificationText) {
        val parsed = BankNotificationParsers.parse(packageName, text)
        if (parsed == null) {
            keepIfAsked(packageName, postedAt, text)
            return
        }
        val payment = DetectedPaymentEntity(
            id = UUID.randomUUID(),
            dedupKey = "$packageName|$notificationKey|${postedAt.toEpochMilli()}|${parsed.amount}",
            sourcePackage = packageName,
            postedAt = postedAt,
            amount = parsed.amount.toDouble(),
            currency = parsed.currency,
            direction = parsed.direction.name,
            payee = parsed.payee,
            categoryHint = parsed.categoryHint,
            status = DetectedPaymentStatus.NEW,
            transactionId = null,
            createdAt = timeProvider.utcNow(),
        )
        // Seen before, e.g. the notification was updated or Kissos reconnected
        if (dao.insertPayment(payment) == -1L) return

        val autoAdd = prefs.apps.first().find { it.packageName == packageName }?.autoAdd == true
        val draft = payments.draft(payment)
        if (autoAdd && payments.add(payment.id)) {
            notifier.showAdded(draft)
        } else {
            notifier.showDetected(draft)
        }
    }

    /** Old handled payments and samples go; the payments still waiting stay until the user decides. */
    suspend fun purge() {
        val now = timeProvider.utcNow()
        dao.deleteHandledBefore(now - KEEP_HANDLED)
        dao.deleteUnparsedBefore(now - KEEP_UNPARSED)
    }

    private suspend fun keepIfAsked(packageName: String, postedAt: Instant, text: NotificationText) {
        val body = text.lines.joinToString("\n")
        if (body.isEmpty() || !prefs.keepUnparsed.first() || dao.hasUnparsed(packageName, postedAt, body)) return
        dao.insertUnparsed(
            UnparsedNotificationEntity(
                id = UUID.randomUUID(),
                sourcePackage = packageName,
                title = text.title,
                text = body,
                postedAt = postedAt,
            )
        )
    }

    companion object {
        /** Long enough to recognise a notification shown again, e.g. after a restart. */
        private val KEEP_HANDLED: Duration = Duration.ofDays(30)
        val KEEP_UNPARSED: Duration = Duration.ofDays(7)
    }
}

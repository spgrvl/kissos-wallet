package com.spgrvl.kissos.data.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * A payment a bank app's notification announced, waiting for the user or already handled.
 * Only what was parsed is kept, never the notification's text. Not in backups: the transactions are.
 */
@Keep
@Entity(
    tableName = "detected_payments",
    indices = [Index(value = ["dedupKey"], unique = true)],
)
data class DetectedPaymentEntity(
    @PrimaryKey
    val id: UUID,
    /** Unique per notification, so that one shown again (updated, or after a restart) counts once. */
    val dedupKey: String,
    val sourcePackage: String,
    val postedAt: Instant,
    val amount: Double,
    /** ISO 4217, e.g. EUR. */
    val currency: String,
    /** OUT for a payment, IN for money received. */
    val direction: String,
    val payee: String?,
    /** The bank's own category for it, e.g. Revolut's "❤️". */
    val categoryHint: String?,
    val status: DetectedPaymentStatus,
    /** The transaction it became, if it was added. */
    val transactionId: UUID?,
    val createdAt: Instant,
    @ColumnInfo(defaultValue = "PAYMENT")
    val kind: DetectedPaymentKind,
    /** For an account debit: the payment detected earlier that it's the same as ([DetectedPaymentStatus.MATCHED]). */
    val matchedPaymentId: UUID?,
)

enum class DetectedPaymentKind {
    /** The payment itself, e.g. a card payment announced as it happens. */
    PAYMENT,

    /**
     * Money taken from the account, announced when it's booked: a card payment's comes days after the payment;
     * a payment without the card, e.g. a standing order, only has this one.
     */
    ACCOUNT_DEBIT,
}

enum class DetectedPaymentStatus {
    /** Waiting for the user to add or dismiss it. */
    NEW,
    ADDED,
    DISMISSED,

    /** An account debit for a payment detected earlier: nothing to add, it's already counted. */
    MATCHED,
}

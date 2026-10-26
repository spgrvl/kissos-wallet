package com.ivy.data.model

import java.math.BigDecimal
import java.time.Instant

/** A scanned Greek cash-register receipt, attached to the transaction it was saved as. */
@Suppress("DataClassTypedIDs")
data class Receipt(
    val transactionId: TransactionId,
    /** The raw SIG value from the QR code. */
    val sig: String,
    val registerId: String,
    val receiptNumber: Long,
    val total: BigDecimal,
    /** AADE's issue time once fetched, otherwise the QR code's time hint. */
    val issuedAt: Instant?,
    val merchantAfm: Afm?,
    /** Legal name as AADE shows it, often truncated. */
    val merchantName: String?,
    val address: String?,
    val documentType: String?,
    /** Paid by card according to AADE. Unreliable, a hint only. */
    val cardAmount: BigDecimal?,
    /** Keyed by VAT category letter (Α, Β, Γ, Δ, Ε). */
    val vat: Map<Char, VatAmount>,
    val status: ReceiptStatus,
    /** AADE's message for a rejected receipt, or why its page wasn't recognized. */
    val aadeMessage: String?,
    val scannedAt: Instant,
    val lastCheckedAt: Instant?,
) {
    val id: ReceiptId get() = ReceiptId(registerId, receiptNumber)
}

/** What makes a receipt unique: the cash register and its receipt counter. */
@Suppress("DataClassTypedIDs")
data class ReceiptId(val registerId: String, val receiptNumber: Long)

data class VatAmount(val net: BigDecimal, val vat: BigDecimal)

enum class ReceiptStatus {
    /** AADE has the receipt and its details are filled in. */
    FOUND,

    /** Not looked up yet, offline, or not yet transmitted to AADE. Retried in the background. */
    PENDING,

    /** AADE rejected the code. */
    REJECTED,

    /** AADE's page had an unknown layout. */
    UNRECOGNIZED,
}

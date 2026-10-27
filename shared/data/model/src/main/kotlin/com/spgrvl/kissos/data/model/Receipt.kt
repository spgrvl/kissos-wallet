package com.spgrvl.kissos.data.model

import java.math.BigDecimal
import java.time.Instant

/**
 * A scanned Greek receipt, attached to the transaction it was saved as. Either a cash-register (ΦΗΜ)
 * receipt or a document sent to myDATA, e.g. through an e-invoicing provider.
 */
@Suppress("DataClassTypedIDs")
data class Receipt(
    val transactionId: TransactionId,
    /** What it's looked up with: the SIG value of a cash-register receipt, or the myDATA link. */
    val sig: String,
    /** The cash register, or the series of a myDATA document. */
    val registerId: String?,
    val receiptNumber: Long?,
    /** myDATA's unique number (ΜΑΡΚ). Null for cash-register receipts. */
    val mark: Long?,
    /** In a cash-register receipt's QR code; only known from AADE for myDATA documents. */
    val total: BigDecimal?,
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
)

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

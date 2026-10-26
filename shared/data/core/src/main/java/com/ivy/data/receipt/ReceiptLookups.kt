package com.ivy.data.receipt

import com.ivy.data.model.Afm
import com.ivy.data.model.Receipt
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.model.TransactionId
import com.ivy.data.model.VatAmount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** AADE's pages and the registers' time hints are in Greek local time. */
val GREEK_TIME_ZONE: ZoneId = ZoneId.of("Europe/Athens")

fun LocalDateTime.greekTimeToInstant(): Instant = atZone(GREEK_TIME_ZONE).toInstant()

/** Receipt amounts are euros with cents. */
fun BigDecimal.toReceiptMoney(): BigDecimal = setScale(2, RoundingMode.HALF_UP)

fun Double.toReceiptMoney(): BigDecimal = toBigDecimal().toReceiptMoney()

/** What is known from the QR code alone, before AADE has been asked. */
fun AadeQr.toPendingReceipt(transactionId: TransactionId, scannedAt: Instant): Receipt = Receipt(
    transactionId = transactionId,
    sig = sig,
    registerId = registerId,
    receiptNumber = receiptNumber,
    total = total.toReceiptMoney(),
    issuedAt = issuedAtHint?.greekTimeToInstant(),
    merchantAfm = null,
    merchantName = null,
    address = null,
    documentType = null,
    cardAmount = null,
    vat = emptyMap(),
    status = ReceiptStatus.PENDING,
    aadeMessage = null,
    scannedAt = scannedAt,
    lastCheckedAt = null,
)

/** Updates the receipt with what AADE answered. */
fun Receipt.withLookup(lookup: AadeLookup, checkedAt: Instant): Receipt = when (lookup) {
    is AadeLookup.Found -> copy(
        issuedAt = lookup.receipt.issuedAt.greekTimeToInstant(),
        merchantAfm = Afm.from(lookup.receipt.merchantAfm).getOrNull(),
        merchantName = lookup.receipt.merchantName,
        address = lookup.receipt.address,
        documentType = lookup.receipt.documentType,
        cardAmount = lookup.receipt.cardAmount.toReceiptMoney(),
        vat = lookup.receipt.vatAmounts(),
        status = ReceiptStatus.FOUND,
        aadeMessage = null,
        lastCheckedAt = checkedAt,
    )

    is AadeLookup.NotYetTransmitted -> copy(
        merchantAfm = lookup.merchantAfm?.let { Afm.from(it).getOrNull() } ?: merchantAfm,
        merchantName = lookup.merchantName ?: merchantName,
        address = lookup.address ?: address,
        status = ReceiptStatus.PENDING,
        aadeMessage = null,
        lastCheckedAt = checkedAt,
    )

    is AadeLookup.Rejected -> copy(
        status = ReceiptStatus.REJECTED,
        aadeMessage = lookup.message,
        lastCheckedAt = checkedAt,
    )

    is AadeLookup.Unrecognized -> copy(
        status = ReceiptStatus.UNRECOGNIZED,
        aadeMessage = lookup.reason,
        lastCheckedAt = checkedAt,
    )
}

private fun AadeReceipt.vatAmounts(): Map<Char, VatAmount> =
    (netByVatCategory.keys + vatByVatCategory.keys)
        .associateWith { category ->
            VatAmount(
                net = (netByVatCategory[category] ?: BigDecimal.ZERO).toReceiptMoney(),
                vat = (vatByVatCategory[category] ?: BigDecimal.ZERO).toReceiptMoney(),
            )
        }
        .filterValues { it.net.signum() != 0 || it.vat.signum() != 0 }

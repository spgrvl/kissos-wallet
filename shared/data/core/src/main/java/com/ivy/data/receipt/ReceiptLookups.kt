package com.ivy.data.receipt

import com.ivy.data.model.Afm
import com.ivy.data.model.Receipt
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.model.TransactionId
import com.ivy.data.model.VatAmount
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** AADE's pages and the registers' time hints are in Greek local time. */
val GREEK_TIME_ZONE: ZoneId = ZoneId.of("Europe/Athens")

fun LocalDateTime.greekTimeToInstant(): Instant = atZone(GREEK_TIME_ZONE).toInstant()

/** Receipt amounts are euros with cents. */
fun BigDecimal.toReceiptMoney(): BigDecimal = setScale(2, RoundingMode.HALF_UP)

fun Double.toReceiptMoney(): BigDecimal = toBigDecimal().toReceiptMoney()

/** What is known from the QR code alone, before AADE has been asked. */
fun ReceiptCode.Fetchable.toPendingReceipt(transactionId: TransactionId, scannedAt: Instant): Receipt =
    when (this) {
        is ReceiptCode.CashRegister -> qr.toPendingReceipt(transactionId, scannedAt)
        is ReceiptCode.MyData, is ReceiptCode.FiscalSign -> Receipt(
            transactionId = transactionId,
            sig = key,
            registerId = null,
            receiptNumber = null,
            mark = null,
            total = null,
            issuedAt = null,
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
    }

fun AadeQr.toPendingReceipt(transactionId: TransactionId, scannedAt: Instant): Receipt = Receipt(
    transactionId = transactionId,
    sig = sig,
    registerId = registerId,
    receiptNumber = receiptNumber,
    mark = null,
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
        registerId = lookup.receipt.registerId ?: registerId,
        receiptNumber = lookup.receipt.receiptNumber ?: receiptNumber,
        mark = lookup.receipt.mark ?: mark,
        // A cash-register receipt keeps the amount in its QR code
        total = total ?: lookup.receipt.total.toReceiptMoney(),
        issuedAt = lookup.receipt.issuedAt?.greekTimeToInstant() ?: issuedOn(lookup.receipt.issuedOn),
        merchantAfm = Afm.from(lookup.receipt.merchantAfm).getOrNull(),
        merchantName = lookup.receipt.merchantName ?: merchantName,
        address = lookup.receipt.address,
        documentType = lookup.receipt.documentType,
        cardAmount = lookup.receipt.cardAmount?.toReceiptMoney(),
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

/**
 * When AADE only knows the date: the time of scanning if that was the same day,
 * since receipts are usually scanned right after paying, otherwise midday.
 */
private fun Receipt.issuedOn(date: LocalDate): Instant {
    val scanned = scannedAt.atZone(GREEK_TIME_ZONE)
    return if (scanned.toLocalDate() == date) {
        scannedAt
    } else {
        date.atTime(LocalTime.NOON).greekTimeToInstant()
    }
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

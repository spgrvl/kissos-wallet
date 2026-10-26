package com.ivy.data.receipt

import com.ivy.data.model.Afm
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.model.TransactionId
import com.ivy.data.model.VatAmount
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ReceiptLookupsTest {
    private val transactionId = TransactionId(UUID.randomUUID())
    private val scannedAt = Instant.parse("2026-10-22T16:00:00Z")
    private val checkedAt = Instant.parse("2026-10-22T16:00:05Z")

    private fun pending(receipt: AadeTestReceipt) =
        AadeQr.parse(receipt.qr)!!.toPendingReceipt(transactionId, scannedAt)

    @Test
    fun `a scanned QR becomes a pending receipt with the time hint in Greek time`() {
        // when
        val receipt = pending(AadeTestReceipt.BARBER)

        // then
        receipt.status shouldBe ReceiptStatus.PENDING
        receipt.total shouldBe money("12.00")
        // 18:59 in Athens is 15:59 UTC in October (summer time)
        receipt.issuedAt shouldBe Instant.parse("2026-10-22T15:59:00Z")
        receipt.merchantAfm shouldBe null
    }

    @Test
    fun `a QR without a time hint has no issue time yet`() {
        pending(AadeTestReceipt.SUPERMARKET).issuedAt shouldBe null
    }

    @Test
    fun `found - fills in the merchant, time and VAT`() {
        // when
        val receipt = pending(AadeTestReceipt.GRILL).withLookup(AadePage.parse(aadePage("grill")), checkedAt)

        // then
        receipt.status shouldBe ReceiptStatus.FOUND
        receipt.merchantAfm shouldBe Afm.unsafe("044136828")
        receipt.merchantName shouldBe "ΠΑΝΑΓΟΠΟΥΛΟΣ ΧΡΗΣΤ"
        receipt.issuedAt shouldBe Instant.parse("2026-10-17T17:58:00Z")
        receipt.cardAmount shouldBe money("12.80")
        receipt.vat shouldBe mapOf(
            'Β' to VatAmount(net = money("9.56"), vat = money("1.24")),
            'Γ' to VatAmount(net = money("1.61"), vat = money("0.39")),
        )
        receipt.lastCheckedAt shouldBe checkedAt
    }

    @Test
    fun `not yet transmitted - stays pending but the merchant is known`() {
        // given
        val pending = pending(AadeTestReceipt.BARBER)

        // when
        val receipt = pending.withLookup(AadePage.parse(aadePage("not_transmitted")), checkedAt)

        // then
        receipt.status shouldBe ReceiptStatus.PENDING
        receipt.merchantAfm shouldBe Afm.unsafe("067022059")
        receipt.merchantName shouldBe "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ"
        receipt.issuedAt shouldBe pending.issuedAt
    }

    @Test
    fun `rejected - keeps the message from AADE`() {
        // when
        val receipt = pending(AadeTestReceipt.BARBER).withLookup(AadeLookup.Rejected("Λάθος QR code"), checkedAt)

        // then
        receipt.status shouldBe ReceiptStatus.REJECTED
        receipt.aadeMessage shouldBe "Λάθος QR code"
    }
}

package com.spgrvl.kissos.data.receipt

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(TestParameterInjector::class)
class AadePageTest {

    @Test
    fun `parses the test receipts' pages`(
        @TestParameter receipt: AadeTestReceipt,
    ) {
        // given
        val qr = AadeQr.parse(receipt.qr)!!

        // when
        val lookup = AadePage.parse(aadePage(receipt.fixture))

        // then
        val result = lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt
        result.merchantAfm shouldBe receipt.afm
        result.merchantName shouldBe receipt.merchantName
        result.issuedAt shouldBe at(receipt.issuedAt)
        result.cardAmount shouldBe money(receipt.cardAmount)
        result.payment shouldBe receipt.payment
        result.netByVatCategory.filterValues { it.signum() != 0 } shouldBe receipt.net.mapValues { money(it.value) }
        result.vatByVatCategory.filterValues { it.signum() != 0 } shouldBe receipt.vat.mapValues { money(it.value) }
        // The page agrees with the QR code.
        result.total shouldBe qr.total
        result.registerId shouldBe qr.registerId
        result.receiptNumber shouldBe qr.receiptNumber
        // The VAT breakdown adds up to the total.
        (result.netByVatCategory.values + result.vatByVatCategory.values)
            .fold(BigDecimal.ZERO, BigDecimal::add) shouldBe result.total
    }

    @Test
    fun `parses pages with CRLF line endings`() {
        // when
        val lookup = AadePage.parse(aadePage("grill", crlf = true))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.total shouldBe money("12.80")
    }

    @Test
    fun `not yet transmitted - the merchant is still known`() {
        // when
        val lookup = AadePage.parse(aadePage("not_transmitted"))

        // then
        lookup shouldBe AadeLookup.NotYetTransmitted(
            registerId = "DNX25002295",
            merchantAfm = "067022059",
            merchantName = "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ",
            address = "ΑΛ ΥΨΗΛΑΝΤΟΥ 266 TK:26222 ΠΑΤΡΑ",
        )
    }

    @Test
    fun `unknown register - rejected despite the contradictory 'active' box`() {
        // when
        val lookup = AadePage.parse(aadePage("unknown_register"))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Rejected>().message shouldContain "δεν είναι ενεργή"
    }

    @Test
    fun `malformed code - rejected`() {
        // when
        val lookup = AadePage.parse(aadePage("malformed"))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Rejected>().message shouldContain "Λάθος QR code"
    }

    @Test
    fun `unknown layout - unrecognized, not a crash`() {
        AadePage.parse("<html><body>Service unavailable</body></html>")
            .shouldBeInstanceOf<AadeLookup.Unrecognized>()
    }

    @Test
    fun `receipt page with a missing field - unrecognized`() {
        // given
        val noDate = aadePage("barber").replace("2026-10-22 18:59", "")

        // when
        val lookup = AadePage.parse(noDate)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "date"
    }
}

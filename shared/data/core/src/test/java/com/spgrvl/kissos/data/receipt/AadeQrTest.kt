package com.spgrvl.kissos.data.receipt

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AadeQrTest {

    @Test
    fun `parses the test receipts offline`(
        @TestParameter receipt: AadeTestReceipt,
    ) {
        // when
        val qr = AadeQr.parse(receipt.qr)

        // then
        qr.shouldNotBeNull()
        qr.registerId shouldBe receipt.registerId
        qr.receiptNumber shouldBe receipt.receiptNumber
        qr.total shouldBe money(receipt.total)
        qr.issuedAtHint shouldBe receipt.timeHint?.let(::at)
        qr.lookupUrl shouldBe receipt.qr
    }

    @Test
    fun `parses the example from AADE circular A_1135_2023`() {
        // when
        val qr = AadeQr.parse(
            "https://www1.aade.gr/tameiakes/myweb/q1.php" +
                "?SIG=GRT99000204000003870EA2C079E722C361C6297D10F02955221123103718.35"
        )

        // then
        qr.shouldNotBeNull()
        qr.registerId shouldBe "GRT99000204"
        qr.receiptNumber shouldBe 387L
        qr.total shouldBe money("18.35")
        qr.issuedAtHint shouldBe at("2022-11-23T10:37")
    }

    @Test
    fun `accepts a bare SIG`() {
        AadeQr.parse("DZZ9900000400025460B2B2B2B2B2B2B2B2B2B2B2B2B2B2B2261017205812.80")
            ?.total shouldBe money("12.80")
    }

    @Test
    fun `accepts the old gsis_gr host`() {
        AadeQr.parse(
            "https://www1.gsis.gr/tameiakes/myweb/q1.php" +
                "?SIG=DZZ9900000400025460B2B2B2B2B2B2B2B2B2B2B2B2B2B2B2261017205812.80"
        )?.total shouldBe money("12.80")
    }

    @Test
    fun `rejects codes that are not AADE cash-register receipts`(
        @TestParameter(
            value = [
                "https://example.com/tameiakes/myweb/q1.php" +
                    "?SIG=DZZ9900000400025460B2B2B2B2B2B2B2B2B2B2B2B2B2B2B2261017205812.80",
                "https://www1.aade.gr/myweb/other.php" +
                    "?SIG=DZZ9900000400025460B2B2B2B2B2B2B2B2B2B2B2B2B2B2B2261017205812.80",
                "https://www1.aade.gr/tameiakes/myweb/q1.php" +
                    "?SIG=DZZ99000001000005XXA1A1A1A1A1A1A1A1A1A1A1A1A1A1A1261022185912.00",
                "IRISTIN999000102",
                "hello",
                "",
                "DZZ99000001123",
            ]
        ) text: String,
    ) {
        AadeQr.parse(text).shouldBeNull()
    }
}

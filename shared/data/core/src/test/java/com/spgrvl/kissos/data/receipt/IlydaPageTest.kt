package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class IlydaPageTest {
    private val html = testResource("ilyda/pharmacy.html").readText()

    @Test
    fun `parses an ILYDA document`() {
        // when
        val lookup = IlydaPage.parse(html)

        // then
        lookup shouldBe AadeLookup.Found(
            AadeReceipt(
                registerId = "ΑΠΥΜΒ",
                receiptNumber = 1066,
                mark = 400000000000066,
                total = money("70.00"),
                cardAmount = null,
                issuedAt = LocalDateTime.parse("2026-11-14T09:41"),
                issuedOn = LocalDate.parse("2026-11-14"),
                merchantAfm = "999000066",
                merchantName = "ΔΟΚΙΜΑΣΤΙΚΟ ΦΑΡΜΑΚΕΙΟ ΙΚΕ",
                address = "ΟΔΟΣ ΔΟΚΙΜΗΣ 6 ΑΘΗΝΑ",
                documentType = "ΑΠΥ",
                netByVatCategory = mapOf('Ε' to money("70.00")),
                vatByVatCategory = mapOf('Ε' to money("0.00")),
            )
        )
    }

    @Test
    fun `reads each VAT rate`() {
        // given: two rates in the VAT analysis instead of one
        val lines = SimplyCloudPage.lines(html).toMutableList()
        val start = lines.indexOf("Ποσό Φ.Π.Α.") + 1
        repeat(3) { lines.removeAt(start) }
        lines.addAll(start, listOf("24.00", "50.00", "12.00", "13.00", "20.00", "2.60"))

        // when
        val lookup = IlydaPage.parse(lines)

        // then
        val receipt = lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt
        receipt.netByVatCategory shouldBe mapOf('Γ' to money("50.00"), 'Β' to money("20.00"))
        receipt.vatByVatCategory shouldBe mapOf('Γ' to money("12.00"), 'Β' to money("2.60"))
    }

    @Test
    fun `an error page is unrecognized`() {
        // when
        val lookup = IlydaPage.parse("<html><body>Το παραστατικό δεν βρέθηκε</body></html>")

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "AFM"
    }
}

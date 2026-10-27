package com.ivy.data.receipt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate

class MyDataPageTest {

    @Test
    fun `parses a provider's receipt`() {
        // when
        val lookup = MyDataPage.parse(myDataPage("shell"))

        // then
        lookup shouldBe AadeLookup.Found(
            AadeReceipt(
                registerId = "Υ257",
                receiptNumber = 2180986,
                mark = 400015809661515,
                total = money("74.00"),
                cardAmount = null,
                issuedAt = null,
                issuedOn = LocalDate.parse("2026-10-26"),
                merchantAfm = "094025320",
                merchantName = null,
                address = null,
                documentType = "ΑΛΠ (Απόδειξη Λιανικής Πώλησης)",
                netByVatCategory = emptyMap(),
                vatByVatCategory = emptyMap(),
            )
        )
    }

    @Test
    fun `fills in the issuer's name when AADE shows it`() {
        // given
        val html = myDataPage("shell")
            .replace("id=\"bname\" name=\"bname\" readonly=\"\" value=\"\"", "id=\"bname\" value=\"SHELL &amp; ΣΙΑ\"")

        // when
        val lookup = MyDataPage.parse(html)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.merchantName shouldBe "SHELL & ΣΙΑ"
    }

    @Test
    fun `a cancelled document is rejected`() {
        // given
        val html = myDataPage("shell").replace(
            "<tr id=\"trcancellationMark\" name=\"trcancellationMark\" style=\"color:red;\"></tr>",
            "<tr id=\"trcancellationMark\"><td>Ακυρωμένο με ΜΑΡΚ</td><td>400015809661600</td></tr>",
        )

        // when
        val lookup = MyDataPage.parse(html)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Rejected>().message shouldContain "Ακυρωμένο"
    }

    @Test
    fun `a page without the document's details is unrecognized`() {
        // when
        val lookup = MyDataPage.parse("<html><body>Σφάλμα</body></html>")

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "total"
    }
}

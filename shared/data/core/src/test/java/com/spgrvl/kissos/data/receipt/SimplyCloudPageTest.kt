package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate

class SimplyCloudPageTest {
    private val html = testResource("simplycloud/butcher.html").readText()

    @Test
    fun `parses a SimplyCloud document`() {
        // when
        val lookup = SimplyCloudPage.parse(html)

        // then
        lookup shouldBe AadeLookup.Found(
            AadeReceipt(
                registerId = "ΑΠΛ",
                receiptNumber = 100022,
                mark = 400000000000022,
                total = money("18.30"),
                cardAmount = null,
                issuedAt = null,
                issuedOn = LocalDate.parse("2026-11-14"),
                merchantAfm = "999000022",
                merchantName = "ΔΟΚΙΜΑΣΤΙΚΟ ΚΡΕΟΠΩΛΕΙΟ ΟΕ",
                address = "ΟΔΟΣ ΔΟΚΙΜΗΣ 1 ΑΘΗΝΑ",
                documentType = "ΑΠΟΔΕΙΞΗ ΛΙΑΝΙΚΗΣ ΠΩΛΗΣΗΣ",
                netByVatCategory = mapOf('Β' to money("16.19")),
                vatByVatCategory = mapOf('Β' to money("2.11")),
            )
        )
    }

    @Test
    fun `reads each VAT rate`() {
        // given: a second rate in the VAT analysis
        val rows = Regex("(?s)(<td class=\"text-left\">[^<]*13%</td>.*?</tr>)").find(html)!!.value
        val secondRate = rows.replace("13%", "24%").replace("16,19", "1,00").replace("2,11", "0,24")
        val twoRates = html.replace(rows, rows + secondRate)

        // when
        val lookup = SimplyCloudPage.parse(twoRates)

        // then
        val receipt = lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt
        receipt.netByVatCategory shouldBe mapOf('Β' to money("16.19"), 'Γ' to money("1.00"))
        receipt.vatByVatCategory shouldBe mapOf('Β' to money("2.11"), 'Γ' to money("0.24"))
    }

    @Test
    fun `an error page is unrecognized`() {
        // when
        val lookup = SimplyCloudPage.parse("<html><body>Το παραστατικό δεν βρέθηκε</body></html>")

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "AFM"
    }
}

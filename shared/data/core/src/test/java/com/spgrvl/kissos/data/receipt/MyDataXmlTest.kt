package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate

class MyDataXmlTest {
    private val xml = testResource("invoiceportal/gyros.xml").readText()

    @Test
    fun `parses an invoiceportal document`() {
        // when
        val lookup = MyDataXml.parse(xml)

        // then
        lookup shouldBe AadeLookup.Found(
            AadeReceipt(
                registerId = "0",
                receiptNumber = 101011,
                mark = 400000000000011,
                total = money("11.40"),
                cardAmount = money("0"),
                issuedAt = null,
                issuedOn = LocalDate.parse("2026-11-12"),
                merchantAfm = "999000011",
                merchantName = null,
                address = null,
                documentType = "ΑΛΠ (Απόδειξη Λιανικής Πώλησης)",
                netByVatCategory = mapOf('Β' to money("10.09")),
                vatByVatCategory = mapOf('Β' to money("1.31")),
            )
        )
    }

    @Test
    fun `paid in cash is not paid by card`() {
        MyDataXml.parse(xml).shouldBeInstanceOf<AadeLookup.Found>().receipt.payment shouldBe
            AadeReceipt.Payment.NOT_CARD
    }

    @Test
    fun `paid by POS is paid by card`() {
        // given
        val byCard = xml.replace("<type>3</type>", "<type>7</type>")

        // when
        val lookup = MyDataXml.parse(byCard)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.payment shouldBe AadeReceipt.Payment.CARD
    }

    @Test
    fun `reads elements with a namespace prefix`() {
        // given
        val prefixed = xml.replace("<", "<inv:").replace("<inv:/", "</inv:").replace("<inv:?", "<?")

        // when
        val lookup = MyDataXml.parse(prefixed)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.total shouldBe money("11.40")
    }

    @Test
    fun `a document without its total is unrecognized`() {
        // given
        val withoutTotal = xml.replace(Regex("<totalGrossValue>.*</totalGrossValue>"), "")

        // when
        val lookup = MyDataXml.parse(withoutTotal)

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "total"
    }

    @Test
    fun `an error page is unrecognized`() {
        MyDataXml.parse("<html>Not found</html>").shouldBeInstanceOf<AadeLookup.Unrecognized>()
    }
}

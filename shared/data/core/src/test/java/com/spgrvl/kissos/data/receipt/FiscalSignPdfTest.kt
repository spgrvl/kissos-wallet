package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate

class FiscalSignPdfTest {
    private val pdf = testResource("fiscalsign/bakery.pdf").readBytes()

    @Test
    fun `reads the PDF's text line by line`() {
        // when
        val lines = PdfText.lines(pdf)

        // then
        lines.first() shouldBe "SWEETS AND BREAD Ε Ε ΑΦΜ:  802231229"
        lines shouldContain "Σειρά/Αριθμός: 15   127003 Ημ/νία Έκδοσης: 23/10/2026"
        lines shouldContain "13,00 1,15 0,15 1,30"
        lines shouldContain "Τελική Αξία: 1,30"
    }

    @Test
    fun `parses a Fiscal Sign receipt`() {
        // when
        val lookup = FiscalSignPdf.parse(pdf)

        // then
        lookup shouldBe AadeLookup.Found(
            AadeReceipt(
                registerId = "15",
                receiptNumber = 127003,
                mark = 400015798805805,
                total = money("1.30"),
                cardAmount = null,
                issuedAt = null,
                issuedOn = LocalDate.parse("2026-10-23"),
                merchantAfm = "802231229",
                merchantName = "SWEETS AND BREAD Ε Ε",
                address = null,
                documentType = "ΑΛΠ",
                netByVatCategory = mapOf('Β' to money("1.15")),
                vatByVatCategory = mapOf('Β' to money("0.15")),
            )
        )
    }

    @Test
    fun `a document without the receipt's details is unrecognized`() {
        // when
        val lookup = FiscalSignPdf.parse(listOf("Σφάλμα"))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Unrecognized>().reason shouldContain "AFM"
    }

    @Test
    fun `something that isn't a PDF is unrecognized`() {
        FiscalSignPdf.parse("<html>Not found</html>".toByteArray()).shouldBeInstanceOf<AadeLookup.Unrecognized>()
    }
}

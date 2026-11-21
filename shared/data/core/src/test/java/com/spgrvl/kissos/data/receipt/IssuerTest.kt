package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class IssuerTest {
    private val ubl = testResource("invoiceportal/gyros-ubl.xml").readText()
    private val myDataXml = testResource("invoiceportal/gyros.xml").readText()

    @Test
    fun `reads the seller of a UBL invoice, not the buyer`() {
        UblInvoice.supplier(ubl) shouldBe Issuer(afm = "999000011", name = "TEST GRILL Ε Ε")
    }

    @Test
    fun `a UBL invoice without its seller has no issuer`() {
        // given
        val withoutSeller = ubl.replace(
            Regex("(?s)<cac:AccountingSupplierParty>.*</cac:AccountingSupplierParty>"),
            "",
        )

        // then
        UblInvoice.supplier(withoutSeller).shouldBeNull()
    }

    @Test
    fun `names the issuer of the document`() {
        // when
        val lookup = MyDataXml.parse(myDataXml).withIssuer(UblInvoice.supplier(ubl))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.merchantName shouldBe "TEST GRILL Ε Ε"
    }

    @Test
    fun `doesn't name the issuer after someone with another AFM`() {
        // when
        val lookup = MyDataXml.parse(myDataXml).withIssuer(Issuer(afm = "999999999", name = "ΑΛΛΟΣ"))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.merchantName.shouldBeNull()
    }

    @Test
    fun `keeps the name the document already has`() {
        // given
        val named = AadeLookup.Found(
            MyDataXml.parse(myDataXml).shouldBeInstanceOf<AadeLookup.Found>().receipt.copy(merchantName = "ΚΑΤΑΣΤΗΜΑ")
        )

        // when
        val lookup = named.withIssuer(Issuer(afm = "999000011", name = "TEST GRILL Ε Ε"))

        // then
        lookup.shouldBeInstanceOf<AadeLookup.Found>().receipt.merchantName shouldBe "ΚΑΤΑΣΤΗΜΑ"
    }
}

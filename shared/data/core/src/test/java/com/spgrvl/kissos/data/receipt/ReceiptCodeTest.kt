package com.spgrvl.kissos.data.receipt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class ReceiptCodeTest {

    @Test
    fun `a cash-register receipt's link`() {
        // when
        val code = ReceiptCode.parse(AadeTestReceipt.GRILL.qr)

        // then
        code.shouldBeInstanceOf<ReceiptCode.CashRegister>().key shouldBe AadeQr.parse(AadeTestReceipt.GRILL.qr)!!.sig
    }

    @Test
    fun `a myDATA link is looked up as it is`() {
        // when
        val code = ReceiptCode.parse(ShellMyDataUrl)

        // then
        code shouldBe ReceiptCode.MyData(ShellMyDataUrl)
        ReceiptCode.fromKey(ShellMyDataUrl) shouldBe code
    }

    @Test
    fun `a myDATA link over http is looked up over https`() {
        // when
        val code = ReceiptCode.parse(ShellMyDataUrl.replace("https://", "http://"))

        // then
        code shouldBe ReceiptCode.MyData(ShellMyDataUrl)
    }

    @Test
    fun `a myDATA link from a provider's page is the same as the printed one`() {
        // given: Epsilon's page links with lowercase escapes
        val fromProvider = ShellMyDataUrl.replace("%2F", "%2f").replace("%3D", "%3d")

        // when
        val code = ReceiptCode.parse(fromProvider)

        // then
        code shouldBe ReceiptCode.MyData(ShellMyDataUrl)
    }

    @Test
    fun `a myDATA link without a document is not a receipt`() {
        ReceiptCode.parse("https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo") shouldBe null
    }

    @Test
    fun `an Epsilon Digital page is a provider page`() {
        // given
        val url = "https://epsilondigital6.epsilonnet.gr/fd/65f112109ef14dde635b08df32d26fd5:118"

        // when
        val code = ReceiptCode.parse(url)

        // then
        code shouldBe ReceiptCode.Provider(url)
        ReceiptCode.fromKey(url) shouldBe null
    }

    @Test
    fun `an EntersoftOne page is a provider page`() {
        // given
        val url = "https://e-invoicing.gr/edocuments/ViewInvoice/-1/00000000-0000-0000-0000-000000000000_abcd1234"

        // when
        val code = ReceiptCode.parse(url)

        // then
        code shouldBe ReceiptCode.Provider(url)
        ReceiptCode.fromKey(url) shouldBe null
    }

    @Test
    fun `an Impact page is a provider page`() {
        // given
        val impact = "https://einvoice.impact.gr/p/EL999000044/0123456789ABCDEF0123456789ABCDEF01234567/" +
            "0123456789ABCDEF0123456789ABCDEF"

        // then
        ReceiptCode.parse(impact) shouldBe ReceiptCode.Provider(impact)
    }

    @Test
    fun `an Impact view page is a provider page too`() {
        // given
        val impact = "https://einvoice.impact.gr/v/EL999000044-10000044-0123456789ABCDEF0123456789ABCDEF01234567-" +
            "0123456789ABCDEF0123456789ABCDEF"

        // then
        ReceiptCode.parse(impact) shouldBe ReceiptCode.Provider(impact)
    }

    @Test
    fun `an ILYDA page is read directly`() {
        // given
        val page = "vs.gr/iv/invoice/download/test_1/test_1~k1~fedcba9876543210fedcba9876543210fedcba98~" +
            "AbCdEfGhIjKlMnOpQrStUvWxYz0123456789-_AbCd"

        // when
        val code = ReceiptCode.parse("http://$page")

        // then
        code shouldBe ReceiptCode.Ilyda("https://$page")
        ReceiptCode.fromKey("https://$page") shouldBe code
    }

    @Test
    fun `an Eskap page is read directly`() {
        // given
        val eskap = "https://www.eskap.gr/invoice/abcdefghijklmnopqrstuvwxyz012345"

        // when
        val code = ReceiptCode.parse(eskap)

        // then
        code shouldBe ReceiptCode.Eskap(eskap)
        ReceiptCode.fromKey(eskap) shouldBe code
    }

    @Test
    fun `only a provider's document pages count`() {
        ReceiptCode.parse("https://www.eskap.gr/") shouldBe null
        ReceiptCode.parse("https://www.eskap.gr/invoice/") shouldBe null
        ReceiptCode.parse("https://einvoice.impact.gr/login") shouldBe null
        ReceiptCode.parse("https://vs.gr/") shouldBe null
        ReceiptCode.parse("https://vs.gr/iv/invoice/download/") shouldBe null
    }

    @Test
    fun `an invoiceportal page is downloaded as myDATA XML`() {
        // given
        val url = "https://invoiceportal.gr/invoices/2026-0123456789ABCDEF0123456789ABCDEF01234567"

        // when
        val code = ReceiptCode.parse("$url/")

        // then
        code shouldBe ReceiptCode.InvoicePortal(url)
        code.shouldBeInstanceOf<ReceiptCode.InvoicePortal>().lookupUrl shouldBe "$url/myDATA"
        ReceiptCode.fromKey(url) shouldBe code
    }

    @Test
    fun `an invoiceportal download is not a receipt page`() {
        ReceiptCode.parse("https://invoiceportal.gr/invoices/2026-0123456789ABCDEF/pdf") shouldBe null
    }

    @Test
    fun `a SimplyCloud page is downloaded over https`() {
        // given
        val page = "app.simplycloud.gr/InvoiceData/v/GR999000022-00000000-0000-0000-0000-000000000000-" +
            "FEDCBA9876543210FEDCBA9876543210FEDCBA98"

        // when
        val code = ReceiptCode.parse("http://$page")

        // then
        code shouldBe ReceiptCode.SimplyCloud("https://$page")
        ReceiptCode.fromKey("https://$page") shouldBe code
    }

    @Test
    fun `a Fiscal Sign receipt is downloaded from its link`() {
        // given
        val url = "https://fiscalsign.gr/api/PrintInvoice/download/3f431961-dc73-4878-bf1d-ef17c2c1dbd6"

        // when
        val code = ReceiptCode.parse(url)

        // then
        code shouldBe ReceiptCode.FiscalSign(url)
        ReceiptCode.fromKey(url) shouldBe code
    }

    @Test
    fun `provider and Fiscal Sign links over http use https`() {
        // given
        val provider = "epsilondigital6.epsilonnet.gr/fd/65f112109ef14dde635b08df32d26fd5:118"
        val fiscalSign = "fiscalsign.gr/api/PrintInvoice/download/3f431961-dc73-4878-bf1d-ef17c2c1dbd6"

        // then
        ReceiptCode.parse("http://$provider") shouldBe ReceiptCode.Provider("https://$provider")
        ReceiptCode.parse("HTTP://$fiscalSign") shouldBe ReceiptCode.FiscalSign("https://$fiscalSign")
    }

    @Test
    fun `other codes are not receipts`() {
        ReceiptCode.parse("https://example.com/receipt?q=1") shouldBe null
        ReceiptCode.parse("https://notepsilonnet.gr/fd/1") shouldBe null
        ReceiptCode.parse("WIFI:S:home;T:WPA;P:secret;;") shouldBe null
    }
}

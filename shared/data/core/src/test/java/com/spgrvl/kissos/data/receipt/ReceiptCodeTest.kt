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
    fun `other codes are not receipts`() {
        ReceiptCode.parse("https://example.com/receipt?q=1") shouldBe null
        ReceiptCode.parse("https://notepsilonnet.gr/fd/1") shouldBe null
        ReceiptCode.parse("WIFI:S:home;T:WPA;P:secret;;") shouldBe null
    }
}

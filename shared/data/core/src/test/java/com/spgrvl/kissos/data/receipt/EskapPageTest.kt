package com.spgrvl.kissos.data.receipt

import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test

class EskapPageTest {
    private val html = testResource("eskap/restaurant.html").readText()

    @Test
    fun `reads the issuer and the myDATA link`() {
        // when
        val document = EskapPage.parse(html)

        // then
        document shouldBe EskapPage.Document(
            myData = ReceiptCode.MyData(
                "https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo" +
                    "?q=AAAAAAAAAAAAAAAAAAAA%2FBBBBBBBBBBBBBBBBBBBB%2BCCCCCCCCCCCCCCCCCCCC%3D"
            ),
            issuer = Issuer(afm = "999000033", name = "ΔΟΚΙΜΑΣΤΙΚΗ ΠΙΤΣΑΡΙΑ Ε.Ε."),
        )
    }

    @Test
    fun `an issuer without a name isn't named`() {
        // given
        val unnamed = html.replace("ΔΟΚΙΜΑΣΤΙΚΗ ΠΙΤΣΑΡΙΑ Ε.Ε.", "")

        // when
        val document = EskapPage.parse(unnamed)

        // then
        document?.issuer.shouldBeNull()
    }

    @Test
    fun `a page without a myDATA link is no document`() {
        EskapPage.parse("<html><body>Δεν βρέθηκε</body></html>").shouldBeNull()
    }
}

package com.spgrvl.kissos.base

import io.kotest.matchers.shouldBe
import org.junit.Test

class DefaultCurrencyTest {
    @Test
    fun `country codes map to their currency`() {
        DefaultCurrency.currencyOfCountry("gr") shouldBe "EUR"
        DefaultCurrency.currencyOfCountry("GR") shouldBe "EUR"
        DefaultCurrency.currencyOfCountry("us") shouldBe "USD"
        DefaultCurrency.currencyOfCountry("gb") shouldBe "GBP"
    }

    @Test
    fun `missing or unknown countries give nothing`() {
        DefaultCurrency.currencyOfCountry(null) shouldBe null
        DefaultCurrency.currencyOfCountry("") shouldBe null
        DefaultCurrency.currencyOfCountry("zz") shouldBe null
        DefaultCurrency.currencyOfCountry("GRC") shouldBe null
    }

    @Test
    fun `falls back to EUR without any country`() {
        DefaultCurrency.code(context = null).length shouldBe 3
    }
}

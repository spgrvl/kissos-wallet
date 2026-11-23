package com.spgrvl.kissos.ui

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.Locale

class CurrencyDisplayTest {
    private val greek = Locale.forLanguageTag("el")
    private val english = Locale.forLanguageTag("en")
    private val nbsp = ' '

    @Test
    fun `greek puts the symbol after the number`() {
        CurrencyDisplay.format("23,84", "EUR", greek) shouldBe "23,84$nbsp€"
        CurrencyDisplay.isSymbolFirst(greek) shouldBe false
    }

    @Test
    fun `english puts the symbol before the number`() {
        CurrencyDisplay.format("23.84", "EUR", english) shouldBe "€23.84"
        CurrencyDisplay.format("1,234.50", "USD", english) shouldBe "$1,234.50"
        CurrencyDisplay.format("9.99", "GBP", english) shouldBe "£9.99"
        CurrencyDisplay.isSymbolFirst(english) shouldBe true
    }

    @Test
    fun `sign stays in front of a leading symbol`() {
        CurrencyDisplay.format("-23.84", "EUR", english) shouldBe "-€23.84"
        CurrencyDisplay.format("+5.00", "EUR", english) shouldBe "+€5.00"
        CurrencyDisplay.format("-23,84", "EUR", greek) shouldBe "-23,84$nbsp€"
    }

    @Test
    fun `currencies without a symbol keep their code`() {
        CurrencyDisplay.symbol("CHF", greek) shouldBe "CHF"
        CurrencyDisplay.format("10.00", "CHF", english) shouldBe "CHF${nbsp}10.00"
        CurrencyDisplay.format("10,00", "CHF", greek) shouldBe "10,00${nbsp}CHF"
    }

    @Test
    fun `crypto and unknown codes keep their code`() {
        CurrencyDisplay.symbol("BTC", english) shouldBe "BTC"
        CurrencyDisplay.format("0.5", "BTC", english) shouldBe "BTC${nbsp}0.5"
        CurrencyDisplay.symbol("", english) shouldBe ""
        CurrencyDisplay.format("12", "", english) shouldBe "12"
    }

    @Test
    fun `lowercase codes are recognised`() {
        CurrencyDisplay.symbol("eur", greek) shouldBe "€"
    }

    @Test
    fun `parts keep symbol and number apart for styling`() {
        CurrencyDisplay.parts("-23.84", "EUR", english) shouldBe MoneyParts("-€", "23.84", "")
        CurrencyDisplay.parts("23,84", "EUR", greek) shouldBe MoneyParts("", "23,84", "€")
    }

    @Test
    fun `hidden amounts keep the symbol`() {
        CurrencyDisplay.format("****", "EUR", english) shouldBe "€****"
    }
}

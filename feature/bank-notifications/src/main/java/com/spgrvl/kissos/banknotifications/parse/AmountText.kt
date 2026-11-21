package com.spgrvl.kissos.banknotifications.parse

import java.math.BigDecimal

/**
 * Reads an amount as bank apps write it in notifications: "46,65€", "€24.62", "1.234,56 €", "EUR 12.00".
 * Whichever separator comes last, followed by one or two digits, is the decimal mark.
 */
object AmountText {
    data class Money(val amount: BigDecimal, val currency: String)

    private const val SYMBOL = "[€$£]"
    private const val CODE = "[A-Z]{3}"
    private const val NUMBER = "\\d{1,3}(?:[.,\\u00A0 ]\\d{3})*(?:[.,]\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?"

    /** An amount with its currency before or after it, for parsers to embed in their patterns. */
    const val PATTERN = "(?:(?:$SYMBOL|$CODE)\\s?(?:$NUMBER)|(?:$NUMBER)\\s?(?:$SYMBOL|$CODE))"

    private val MONEY = Regex(PATTERN)
    private val CURRENCY = Regex("$SYMBOL|$CODE")
    private val DIGITS = Regex("\\d[\\d.,\\u00A0 ]*")
    private val DECIMALS = Regex("[.,](\\d{1,2})$")
    private val CURRENCY_BY_SYMBOL = mapOf("€" to "EUR", "$" to "USD", "£" to "GBP")

    /** @return null unless the text is an amount with its currency and nothing else. */
    fun parse(text: String): Money? {
        val money = text.trim().takeIf(MONEY::matches) ?: return null
        val currency = CURRENCY.find(money)?.value?.let { CURRENCY_BY_SYMBOL[it] ?: it }
        val amount = DIGITS.find(money)?.value?.let(::number)
        return if (currency != null && amount != null) Money(amount, currency) else null
    }

    private fun number(text: String): BigDecimal? {
        val compact = text.filter { it.isDigit() || it == '.' || it == ',' }
        val decimals = DECIMALS.find(compact)
        val whole = (if (decimals == null) compact else compact.substring(0, decimals.range.first))
            .filter(Char::isDigit)
        return "$whole.${decimals?.groupValues?.get(1).orEmpty()}".removeSuffix(".").toBigDecimalOrNull()
    }
}

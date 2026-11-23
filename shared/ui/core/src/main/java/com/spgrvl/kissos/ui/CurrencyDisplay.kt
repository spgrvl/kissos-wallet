package com.spgrvl.kissos.ui

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * How an amount and its currency are shown on screen: the currency's symbol (€, $, £)
 * instead of its ISO code, placed where the app language expects it ("23,84 €" in Greek,
 * "€23.84" in English). Currencies without a symbol of their own (CHF, crypto) keep their
 * code. Stored data, exports and backups keep using codes; this is display only.
 */
object CurrencyDisplay {
    private const val NBSP = ' '

    private val symbols = ConcurrentHashMap<Pair<String, Locale>, String>()
    private val symbolFirst = ConcurrentHashMap<Locale, Boolean>()

    /** The symbol for [code] in [locale], or the code itself when there is none. */
    fun symbol(code: String, locale: Locale = Locale.getDefault()): String {
        if (code.isBlank()) return code
        return symbols.getOrPut(code to locale) {
            val currency = runCatching { Currency.getInstance(code.uppercase(Locale.ROOT)) }.getOrNull()
            currency?.getSymbol(locale)?.takeIf { it.isNotBlank() } ?: code
        }
    }

    /** Whether [locale] writes the currency before the number. */
    fun isSymbolFirst(locale: Locale = Locale.getDefault()): Boolean = symbolFirst.getOrPut(locale) {
        val pattern = (NumberFormat.getCurrencyInstance(locale) as? DecimalFormat)
            ?.toPattern()
            ?.substringBefore(';')
            ?: return@getOrPut false
        val currencyAt = pattern.indexOf('¤')
        val numberAt = pattern.indexOfFirst { it == '#' || it == '0' }
        currencyAt >= 0 && (numberAt < 0 || currencyAt < numberAt)
    }

    /**
     * Splits an already formatted amount into what goes before the number, the number and
     * what goes after it, so components can style the symbol and the number differently.
     * A leading sign stays in front of the symbol: "-€23.84", not "€-23.84".
     */
    fun parts(
        amountText: String,
        code: String,
        locale: Locale = Locale.getDefault(),
    ): MoneyParts {
        val symbol = symbol(code, locale)
        if (symbol.isEmpty()) return MoneyParts(prefix = "", number = amountText, suffix = "")
        return if (isSymbolFirst(locale)) {
            val sign = amountText.takeWhile { it == '-' || it == '+' || it == '−' }
            val gap = if (symbol.last().isLetter()) NBSP.toString() else ""
            MoneyParts(prefix = sign + symbol + gap, number = amountText.drop(sign.length), suffix = "")
        } else {
            MoneyParts(prefix = "", number = amountText, suffix = symbol)
        }
    }

    /** The amount with its symbol as one string, e.g. "23,84 €" or "€23.84". */
    fun format(
        amountText: String,
        code: String,
        locale: Locale = Locale.getDefault(),
    ): String = parts(amountText, code, locale).joined()
}

data class MoneyParts(
    val prefix: String,
    val number: String,
    val suffix: String,
) {
    fun joined(): String = buildString {
        append(prefix)
        append(number)
        if (suffix.isNotEmpty()) {
            append(' ')
            append(suffix)
        }
    }
}

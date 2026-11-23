package com.spgrvl.kissos.ui

import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * How an amount and its currency are shown on screen: the currency's symbol (€, $, £) instead
 * of its ISO code, after the number in every language ("23,84 €", "23.84 €"), as the app has
 * always placed the code. The number keeps the language's own separators. Currencies without
 * a symbol of their own (CHF, crypto) keep their code. Stored data, exports and backups keep
 * using codes; this is display only.
 */
object CurrencyDisplay {
    internal const val NBSP = '\u00A0'

    private val symbols = ConcurrentHashMap<Pair<String, Locale>, String>()

    /** The symbol for [code] in [locale], or the code itself when there is none. */
    fun symbol(code: String, locale: Locale = Locale.getDefault()): String {
        if (code.isBlank()) return code
        return symbols.getOrPut(code to locale) {
            val currency = runCatching { Currency.getInstance(code.uppercase(Locale.ROOT)) }.getOrNull()
            currency?.getSymbol(locale)?.takeIf { it.isNotBlank() } ?: code
        }
    }

    /**
     * Splits an already formatted amount into the number and the symbol that follows it, so
     * components can style them differently.
     */
    fun parts(
        amountText: String,
        code: String,
        locale: Locale = Locale.getDefault(),
    ): MoneyParts = MoneyParts(number = amountText, suffix = symbol(code, locale))

    /** The amount with its symbol as one string, e.g. "23,84 €" or "-23.84 €". */
    fun format(
        amountText: String,
        code: String,
        locale: Locale = Locale.getDefault(),
    ): String = parts(amountText, code, locale).joined()
}

data class MoneyParts(
    val number: String,
    val suffix: String,
)

/** The parts as one string; the symbol is kept on the same line as the number. */
fun MoneyParts.joined(): String = buildString {
    append(number)
    if (suffix.isNotEmpty()) {
        append(CurrencyDisplay.NBSP)
        append(suffix)
    }
}

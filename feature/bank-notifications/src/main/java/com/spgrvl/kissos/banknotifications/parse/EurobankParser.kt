package com.spgrvl.kissos.banknotifications.parse

/**
 * Eurobank's card payments, titled "Αγορά/Πληρωμή σε POS":
 *
 * ```
 * Κάνατε αγορά/πληρωμή 19,11 EUR στην εταιρεία TEST MARKET AEBE GR με την κάρτα 1234 56** **** 7890 21/11/2026, 14:47
 * ```
 *
 * The payee is the card terminal's name, which ends with the country code. The card number is never kept.
 */
object EurobankParser : BankNotificationParser {
    override val packages: Set<String> = setOf("com.EurobankEFG")

    private val PAID = Regex(
        "^Κάνατε αγορά/πληρωμή (${AmountText.PATTERN}) στην εταιρεία (.+?) με την κάρτα .*$"
    )

    /** E.g. " GR" at the end of "TEST MARKET AEBE GR". */
    private val COUNTRY_CODE = Regex("\\s+[A-Z]{2}$")

    override fun parse(notification: NotificationText): DetectedPayment? =
        notification.lines.firstNotNullOfOrNull { line ->
            PAID.matchEntire(line)?.let { match ->
                AmountText.parse(match.groupValues[1])?.let { money ->
                    DetectedPayment(
                        amount = money.amount,
                        currency = money.currency,
                        direction = PaymentDirection.OUT,
                        payee = payee(match.groupValues[2]),
                        categoryHint = null,
                    )
                }
            }
        }

    private fun payee(terminal: String): String {
        val name = terminal.trim()
        // Only when something is left: a two-letter name is a name
        return name.replace(COUNTRY_CODE, "").ifEmpty { name }
    }
}

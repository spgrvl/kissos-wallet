package com.spgrvl.kissos.banknotifications.parse

import com.spgrvl.kissos.domain.banknotification.SupportedBankApps

/**
 * Eurobank's card payments, titled "Αγορά/Πληρωμή σε POS":
 *
 * ```
 * Κάνατε αγορά/πληρωμή 19,11 EUR στην εταιρεία TEST MARKET AEBE GR με την κάρτα 1234 56** **** 7890 21/11/2026, 14:47
 * ```
 *
 * and its account debits, titled "Κίνηση Λογαριασμού": a card payment's comes a few days later, when it's
 * booked ([PaymentKind.ACCOUNT_DEBIT]); a payment without the card only has this one:
 *
 * ```
 * 23/11/2026, 15:16: Χρεώθηκε με 19,11 EUR ο λογαριασμός σας με τελευταία ψηφία *1234.
 * Αιτιολογία: TEST MARKET GR. Νέο υπόλοιπο: 100,00 EUR.
 * ```
 *
 * The payee is the card terminal's name or the debit's description, which end with the country code. The card
 * and account numbers and the balance are never kept.
 */
object EurobankParser : BankNotificationParser {
    override val packages: Set<String> = setOf(SupportedBankApps.EUROBANK)

    private val PAID = Regex(
        "^Κάνατε αγορά/πληρωμή (${AmountText.PATTERN}) στην εταιρεία (.+?) με την κάρτα .*$"
    )

    private val DEBITED = Regex(
        "^(?:.*?: )?Χρεώθηκε με (${AmountText.PATTERN}) ο λογαριασμός σας " +
            ".*?Αιτιολογία: (.+?)\\.?(?: Νέο υπόλοιπο:.*)?$"
    )

    /** E.g. " GR" at the end of "TEST MARKET AEBE GR". */
    private val COUNTRY_CODE = Regex("\\s+[A-Z]{2}$")

    override fun parse(notification: NotificationText): DetectedPayment? =
        // The debit's text can come wrapped over lines
        (notification.lines + notification.lines.joinToString(" ")).firstNotNullOfOrNull { line ->
            payment(PAID.matchEntire(line), PaymentKind.PAYMENT)
                ?: payment(DEBITED.matchEntire(line), PaymentKind.ACCOUNT_DEBIT)
        }

    private fun payment(match: MatchResult?, kind: PaymentKind): DetectedPayment? =
        match?.let {
            AmountText.parse(match.groupValues[1])?.let { money ->
                DetectedPayment(
                    amount = money.amount,
                    currency = money.currency,
                    direction = PaymentDirection.OUT,
                    payee = payee(match.groupValues[2]),
                    categoryHint = null,
                    kind = kind,
                )
            }
        }

    private fun payee(terminal: String): String {
        val name = terminal.trim()
        // Only when something is left: a two-letter name is a name
        return name.replace(COUNTRY_CODE, "").ifEmpty { name }
    }
}

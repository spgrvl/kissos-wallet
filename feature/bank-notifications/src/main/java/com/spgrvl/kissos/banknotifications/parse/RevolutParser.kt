package com.spgrvl.kissos.banknotifications.parse

import com.spgrvl.kissos.domain.banknotification.SupportedBankApps

/**
 * Revolut names the payee in the title and starts the text with its category's emoji:
 *
 * ```
 * Test Pharmacy
 * ❤️ You spent €24.62
 * EUR balance: €136.24
 * ```
 */
object RevolutParser : BankNotificationParser {
    override val packages: Set<String> = setOf(SupportedBankApps.REVOLUT)

    /**
     * The category is an emoji, i.e. anything but letters, digits and spaces. Anchored, so that
     * e.g. "You spent €120 last week" isn't taken for a payment.
     */
    private val SPENT = Regex("^(?:([^\\p{L}\\p{N}\\s]+)\\s+)?You spent (${AmountText.PATTERN})\\.?$")

    override fun parse(notification: NotificationText): DetectedPayment? {
        val payee = notification.title?.trim()?.ifEmpty { null } ?: return null
        return notification.lines.firstNotNullOfOrNull { line ->
            SPENT.matchEntire(line)?.let { match ->
                AmountText.parse(match.groupValues[2])?.let { money ->
                    DetectedPayment(
                        amount = money.amount,
                        currency = money.currency,
                        direction = PaymentDirection.OUT,
                        payee = payee,
                        categoryHint = match.groupValues[1].ifEmpty { null },
                    )
                }
            }
        }
    }
}

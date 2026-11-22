package com.spgrvl.kissos.banknotifications.parse

import com.spgrvl.kissos.domain.banknotification.SupportedBankApps

/** Magenta Pay (formerly Payzy) says it all in the text: "Πλήρωσες 46,65€ σε PowerCo." */
object MagentaPayParser : BankNotificationParser {
    override val packages: Set<String> = setOf(SupportedBankApps.MAGENTA_PAY)

    private val PAID = Regex("^Πλήρωσες (${AmountText.PATTERN}) σε (.+?)\\.?$")

    override fun parse(notification: NotificationText): DetectedPayment? =
        notification.lines.firstNotNullOfOrNull { line ->
            PAID.matchEntire(line)?.let { match ->
                AmountText.parse(match.groupValues[1])?.let { money ->
                    DetectedPayment(
                        amount = money.amount,
                        currency = money.currency,
                        direction = PaymentDirection.OUT,
                        payee = match.groupValues[2].trim(),
                        categoryHint = null,
                    )
                }
            }
        }
}

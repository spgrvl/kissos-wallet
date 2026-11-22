package com.spgrvl.kissos.banknotifications.parse

import java.math.BigDecimal

/**
 * The text of a notification from a bank app the user picked. Notifications from other apps are
 * dropped before their text is read, so they never become one of these.
 */
data class NotificationText(
    val title: String?,
    val text: String?,
    /** The whole text when the notification is expanded; [text] can be only its first line. */
    val bigText: String?,
) {
    val lines: List<String>
        get() = (bigText ?: text).orEmpty().lines().map(String::trim).filter(String::isNotEmpty)
}

enum class PaymentDirection { OUT, IN }

/** A payment as a bank app's notification announced it. */
data class DetectedPayment(
    val amount: BigDecimal,
    /** ISO 4217, e.g. EUR. */
    val currency: String,
    val direction: PaymentDirection,
    /** Who was paid, as the bank names them, e.g. "Test Pharmacy". */
    val payee: String?,
    /** The bank's own category for the payment, if it shows one, e.g. Revolut's "❤️" for health. */
    val categoryHint: String?,
)

/** Reads the payments out of one bank app's notifications. */
interface BankNotificationParser {
    /** The app's package names. */
    val packages: Set<String>

    /** @return null for anything that isn't a completed payment, e.g. a declined one, a code or an offer. */
    fun parse(notification: NotificationText): DetectedPayment?
}

object BankNotificationParsers {
    val all: List<BankNotificationParser> = listOf(RevolutParser, MagentaPayParser, EurobankParser)

    val supportedPackages: Set<String> = all.flatMap { it.packages }.toSet()

    fun parse(packageName: String, notification: NotificationText): DetectedPayment? =
        all.firstOrNull { packageName in it.packages }?.parse(notification)
}

package com.spgrvl.kissos.banknotifications.parse

import com.spgrvl.kissos.domain.banknotification.SupportedBankApps
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test

class BankNotificationParsersTest {
    private val revolut = "com.revolut.revolut"
    private val magentaPay = "gr.payzy"
    private val eurobank = "com.EurobankEFG"

    private fun spent(amount: String, payee: String, hint: String? = null) = DetectedPayment(
        amount = amount.toBigDecimal(),
        currency = "EUR",
        direction = PaymentDirection.OUT,
        payee = payee,
        categoryHint = hint,
    )

    @Test
    fun `every app suggested as supported has a parser`() {
        BankNotificationParsers.supportedPackages shouldBe SupportedBankApps.packages
    }

    @Test
    fun `reads Magenta Pay payments`() {
        BankNotificationParsers.parse(magentaPay, text("Πλήρωσες 8,80€ σε Pizza Test.")) shouldBe
            spent("8.80", "Pizza Test")
        BankNotificationParsers.parse(magentaPay, text("Πλήρωσες 46,65€ σε PowerCo.")) shouldBe
            spent("46.65", "PowerCo")
        BankNotificationParsers.parse(magentaPay, text("Πλήρωσες 19,51€ σε FoodApp.")) shouldBe
            spent("19.51", "FoodApp")
    }

    @Test
    fun `reads Magenta Pay payments over a thousand`() {
        BankNotificationParsers.parse(magentaPay, text("Πλήρωσες 1.234,56€ σε Κωτσόβολος.")) shouldBe
            spent("1234.56", "Κωτσόβολος")
    }

    @Test
    fun `reads Revolut payments with their category`() {
        // given
        val pharmacy = NotificationText(
            title = "Φαρμακείο Δοκιμή",
            text = "❤️ You spent €3.20",
            bigText = "❤️ You spent €3.20\nEUR balance: €133.04",
        )
        val marina = NotificationText(
            title = "Test Pharmacy",
            text = "❤️ You spent €24.62",
            bigText = "❤️ You spent €24.62\nEUR balance: €136.24",
        )

        // then
        BankNotificationParsers.parse(revolut, pharmacy) shouldBe
            spent("3.20", "Φαρμακείο Δοκιμή", hint = "❤️")
        BankNotificationParsers.parse(revolut, marina) shouldBe spent("24.62", "Test Pharmacy", hint = "❤️")
    }

    @Test
    fun `reads Revolut payments without the expanded text or a category`() {
        BankNotificationParsers.parse(
            revolut,
            NotificationText(title = "Lidl", text = "You spent €1,234.50", bigText = null),
        ) shouldBe spent("1234.50", "Lidl")
    }

    @Test
    fun `reads Revolut payments in another currency`() {
        BankNotificationParsers.parse(
            revolut,
            NotificationText(title = "Amazon", text = "🛍️ You spent $10.99", bigText = null),
        ) shouldBe spent("10.99", "Amazon", hint = "🛍️").copy(currency = "USD")
    }

    @Test
    fun `the balance is not a payment`() {
        BankNotificationParsers.parse(
            revolut,
            NotificationText(title = "Revolut", text = "EUR balance: €136.24", bigText = null),
        ).shouldBeNull()
    }

    @Test
    fun `other notifications are not payments`() {
        BankNotificationParsers.parse(
            revolut,
            NotificationText(title = "Weekly summary", text = "You spent €120.00 last week", bigText = null),
        ).shouldBeNull()
        BankNotificationParsers.parse(
            revolut,
            NotificationText(title = "Test Pharmacy", text = "❤️ Payment of €24.62 declined", bigText = null),
        ).shouldBeNull()
        BankNotificationParsers.parse(revolut, NotificationText(null, "❤️ You spent €3.20", null)).shouldBeNull()
        BankNotificationParsers.parse(magentaPay, text("Ο κωδικός σου είναι 123456")).shouldBeNull()
        BankNotificationParsers.parse(magentaPay, text("Κέρδισες 5€ σε κουπόνια!")).shouldBeNull()
    }

    @Test
    fun `reads Eurobank card payments`() {
        // given
        val pos = NotificationText(
            title = "Αγορά/Πληρωμή σε POS",
            text = "Κάνατε αγορά/πληρωμή 19,11 EUR στην εταιρεία TEST MARKET AEBE GR " +
                "με την κάρτα 1234 56** **** 7890 21/11/2026, 14:47",
            bigText = null,
        )

        // then: without the country code, and nothing of the card
        BankNotificationParsers.parse(eurobank, pos) shouldBe spent("19.11", "TEST MARKET AEBE")
    }

    @Test
    fun `reads Eurobank payments over a thousand and in other currencies`() {
        BankNotificationParsers.parse(
            eurobank,
            eurobankText("Κάνατε αγορά/πληρωμή 1.234,50 EUR στην εταιρεία ONLINE SHOP IE με την κάρτα 1234 56** 7890"),
        ) shouldBe spent("1234.50", "ONLINE SHOP")
        BankNotificationParsers.parse(
            eurobank,
            eurobankText("Κάνατε αγορά/πληρωμή 9,99 USD στην εταιρεία APP STORE US με την κάρτα 1234 56** **** 7890"),
        ) shouldBe spent("9.99", "APP STORE").copy(currency = "USD")
    }

    @Test
    fun `other Eurobank notifications are not payments`() {
        BankNotificationParsers.parse(eurobank, eurobankText("Ο κωδικός μιας χρήσης είναι 123456")).shouldBeNull()
        BankNotificationParsers.parse(
            eurobank,
            eurobankText("Η συναλλαγή 19,11 EUR στην εταιρεία TEST MARKET AEBE GR απορρίφθηκε"),
        ).shouldBeNull()
    }

    @Test
    fun `apps without a parser have no payments`() {
        BankNotificationParsers.parse("com.example.chat", text("Πλήρωσες 8,80€ σε Pizza Test.")).shouldBeNull()
    }

    private fun text(text: String) = NotificationText(title = "Magenta Pay", text = text, bigText = null)

    private fun eurobankText(text: String) = NotificationText(title = "Eurobank", text = text, bigText = null)
}

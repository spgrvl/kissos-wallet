package com.spgrvl.kissos.banknotifications.parse

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AmountTextTest {

    @Test
    @TestParameters("{text: '46,65€', amount: '46.65', currency: EUR}")
    @TestParameters("{text: '€24.62', amount: '24.62', currency: EUR}")
    @TestParameters("{text: '€ 3.2', amount: '3.2', currency: EUR}")
    @TestParameters("{text: '1.234,56 €', amount: '1234.56', currency: EUR}")
    @TestParameters("{text: '€1,234.56', amount: '1234.56', currency: EUR}")
    @TestParameters("{text: '1.234€', amount: '1234', currency: EUR}")
    @TestParameters("{text: '12€', amount: '12', currency: EUR}")
    @TestParameters("{text: 'EUR 12.00', amount: '12.00', currency: EUR}")
    @TestParameters("{text: '12,00 USD', amount: '12.00', currency: USD}")
    @TestParameters("{text: '$10.99', amount: '10.99', currency: USD}")
    @TestParameters("{text: '£7.50', amount: '7.50', currency: GBP}")
    fun `reads amounts`(text: String, amount: String, currency: String) {
        AmountText.parse(text) shouldBe AmountText.Money(amount.toBigDecimal(), currency)
    }

    @Test
    @TestParameters("{text: '46,65'}")
    @TestParameters("{text: '€'}")
    @TestParameters("{text: 'about €5'}")
    @TestParameters("{text: '123456'}")
    fun `rejects anything but an amount with its currency`(text: String) {
        AmountText.parse(text).shouldBeNull()
    }
}

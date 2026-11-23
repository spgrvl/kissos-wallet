package com.spgrvl.kissos.domain.banknotification

import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.fake.FakeBankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentKind
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.model.Account
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.model.Expense
import com.spgrvl.kissos.data.model.PositiveValue
import com.spgrvl.kissos.data.model.TransactionId
import com.spgrvl.kissos.data.model.TransactionMetadata
import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.model.primitive.ColorInt
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.data.model.primitive.PositiveDouble
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.TransactionRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DetectedPaymentsTest {
    private val now = Instant.parse("2026-11-21T12:00:00Z")
    private val account = Account(
        id = AccountId(UUID.randomUUID()),
        name = NotBlankTrimmedString.from("Revolut").getOrNull()!!,
        asset = AssetCode.from("EUR").getOrNull()!!,
        color = ColorInt(0),
        icon = null,
        includeInBalance = true,
        orderNum = 0.0,
    )
    private val health = UUID.randomUUID()

    private val dao = FakeBankNotificationDao()
    private val prefs = mockk<BankNotificationPrefs> {
        every { apps } returns flowOf(listOf(BankApp("com.revolut.revolut", account.id.value.toString(), false, 0)))
    }
    private val accountRepository = mockk<AccountRepository> {
        coEvery { findById(account.id) } returns account
    }
    private val transactionRepository = mockk<TransactionRepository>(relaxed = true) {
        coEvery { findAllByAccountAndBetween(any(), any(), any()) } returns emptyList()
    }
    private val timeProvider = mockk<TimeProvider> { every { utcNow() } returns now }
    private val payments = DetectedPayments(dao, prefs, accountRepository, transactionRepository, timeProvider)

    private fun payment(
        amount: Double = 3.20,
        currency: String = "EUR",
        payee: String = "Φαρμακείο Δοκιμή",
        hint: String? = "❤️",
        postedAt: Instant = now,
        kind: DetectedPaymentKind = DetectedPaymentKind.PAYMENT,
        sourcePackage: String = "com.revolut.revolut",
    ) = DetectedPaymentEntity(
        id = UUID.randomUUID(),
        dedupKey = UUID.randomUUID().toString(),
        sourcePackage = sourcePackage,
        postedAt = postedAt,
        amount = amount,
        currency = currency,
        direction = "OUT",
        payee = payee,
        categoryHint = hint,
        status = DetectedPaymentStatus.NEW,
        transactionId = null,
        createdAt = now,
        kind = kind,
        matchedPaymentId = null,
    )

    private fun debit(amount: Double = 19.11, payee: String = "TEST MARKET", postedAt: Instant = now) =
        payment(
            amount = amount,
            payee = payee,
            hint = null,
            postedAt = postedAt,
            kind = DetectedPaymentKind.ACCOUNT_DEBIT,
        )

    private fun days(n: Long): Duration = Duration.ofDays(n)

    @Test
    fun `a payment in its account's currency can be added directly`() = runTest {
        // when
        val draft = payments.draft(payment())

        // then
        draft.account shouldBe account
        draft.title shouldBe "Φαρμακείο Δοκιμή"
        draft.canAddDirectly.shouldBeTrue()
    }

    @Test
    fun `a payment in another currency needs the user`() = runTest {
        payments.draft(payment(currency = "USD")).canAddDirectly.shouldBeFalse()
    }

    @Test
    fun `a payment the account may already have needs the user`() = runTest {
        // given: e.g. a scanned receipt
        val receiptExpense = Expense(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = now.minusSeconds(600),
            settled = true,
            metadata = TransactionMetadata(recurringRuleId = null, paidForDateTime = null, loanRecordId = null),
            tags = emptyList(),
            value = PositiveValue(PositiveDouble.from(3.20).getOrNull()!!, account.asset),
            account = account.id,
        )
        coEvery { transactionRepository.findAllByAccountAndBetween(account.id, any(), any()) } returns
            listOf(receiptExpense)

        // when
        val draft = payments.draft(payment())

        // then
        draft.possibleDuplicate.shouldBeTrue()
        draft.canAddDirectly.shouldBeFalse()
    }

    @Test
    fun `an account debit matches the card payment of the same amount days before it`() = runTest {
        // given: the card payment on Saturday, its debit on Monday
        val card = payment(amount = 19.11, payee = "TEST MARKET AEBE", postedAt = now - days(2))
        dao.insertPayment(card)

        // then
        payments.matchingPayment(debit())?.id shouldBe card.id
    }

    @Test
    fun `each card payment matches one account debit`() = runTest {
        // given: two equal payments in a week, the first one's debit already matched
        val first = payment(amount = 2.50, postedAt = now - days(3))
        val second = payment(amount = 2.50, postedAt = now - days(2))
        dao.insertPayment(first)
        dao.insertPayment(second)
        dao.insertPayment(
            debit(amount = 2.50).copy(status = DetectedPaymentStatus.MATCHED, matchedPaymentId = first.id)
        )

        // then: the second debit matches the other payment, and a third has nothing left to match
        val secondDebit = debit(amount = 2.50)
        payments.matchingPayment(secondDebit)?.id shouldBe second.id
        dao.insertPayment(secondDebit.copy(status = DetectedPaymentStatus.MATCHED, matchedPaymentId = second.id))
        payments.matchingPayment(debit(amount = 2.50)).shouldBeNull()
    }

    @Test
    fun `an account debit doesn't match another amount, an older payment, another app or a later one`() = runTest {
        dao.insertPayment(payment(amount = 19.12, postedAt = now - days(1)))
        dao.insertPayment(payment(amount = 19.11, postedAt = now - days(8)))
        dao.insertPayment(payment(amount = 19.11, postedAt = now - days(1), sourcePackage = "gr.payzy"))
        dao.insertPayment(payment(amount = 19.11, postedAt = now + days(1)))

        payments.matchingPayment(debit()).shouldBeNull()
    }

    @Test
    fun `a card payment is never matched as if it were a debit`() = runTest {
        dao.insertPayment(payment(amount = 19.11, postedAt = now - days(1)))

        payments.matchingPayment(payment(amount = 19.11)).shouldBeNull()
    }

    @Test
    fun `an unmatched account debit can be added directly, unless the payee was just paid another amount`() =
        runTest {
            // given: a standing order, never paid by card
            payments.draft(debit(payee = "ΠΑΓΙΑ ΕΝΤΟΛΗ")).canAddDirectly.shouldBeTrue()

            // when: a card payment to the same payee for another amount, e.g. a fuel pre-authorisation
            dao.insertPayment(payment(amount = 50.00, payee = "TEST FUEL STATION", postedAt = now - days(2)))
            val draft = payments.draft(debit(amount = 43.70, payee = "TEST FUEL"))

            // then
            draft.possibleDuplicate.shouldBeTrue()
            draft.canAddDirectly.shouldBeFalse()
        }

    @Test
    fun `undo turns a matched debit into a payment of its own`() = runTest {
        // given
        val card = payment(amount = 19.11, postedAt = now - days(2))
        val matched = debit().copy(status = DetectedPaymentStatus.MATCHED, matchedPaymentId = card.id)
        dao.insertPayment(card)
        dao.insertPayment(matched)

        // when
        payments.undo(matched.id)

        // then
        val undone = dao.findPayment(matched.id)!!
        undone.status shouldBe DetectedPaymentStatus.NEW
        undone.matchedPaymentId.shouldBeNull()
    }

    @Test
    fun `learns the user's name and category for a payee, and the category for the bank's own`() = runTest {
        // given
        payments.learn(payment(payee = "Test Pharmacy"), title = "Pharmacy", categoryId = health)

        // when: the same payee, and another one with the same bank category
        val samePayee = payments.draft(payment(payee = "TEST  PHARMACY "))
        val sameHint = payments.draft(payment(payee = "Another pharmacy"))

        // then
        samePayee.title shouldBe "Pharmacy"
        samePayee.categoryId shouldBe CategoryId(health)
        sameHint.title shouldBe "Another pharmacy"
        sameHint.categoryId shouldBe CategoryId(health)
    }

    @Test
    fun `adding saves an expense and marks the payment added, once`() = runTest {
        // given
        val payment = payment()
        dao.insertPayment(payment)
        val saved = slot<Expense>()
        coEvery { transactionRepository.save(capture(saved)) } returns Unit

        // when
        val added = payments.add(payment.id)
        val addedAgain = payments.add(payment.id)

        // then
        added.shouldBeTrue()
        addedAgain.shouldBeFalse()
        saved.captured.value.amount.value shouldBe 3.20
        saved.captured.account shouldBe account.id
        saved.captured.time shouldBe now
        dao.findPayment(payment.id)?.status shouldBe DetectedPaymentStatus.ADDED
        dao.findPayment(payment.id)?.transactionId shouldBe saved.captured.id.value
        coVerify(exactly = 1) { transactionRepository.save(any()) }
    }

    @Test
    fun `undo removes the expense and puts the payment back`() = runTest {
        // given
        val payment = payment()
        dao.insertPayment(payment)
        payments.add(payment.id)
        val transactionId = dao.findPayment(payment.id)?.transactionId!!

        // when
        payments.undo(payment.id)

        // then
        coVerify { transactionRepository.deleteById(TransactionId(transactionId)) }
        dao.findPayment(payment.id).shouldBeInstanceOf<DetectedPaymentEntity>().status shouldBe
            DetectedPaymentStatus.NEW
    }
}

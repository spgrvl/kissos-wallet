package com.spgrvl.kissos.domain.banknotification

import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.fake.FakeBankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
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
    ) = DetectedPaymentEntity(
        id = UUID.randomUUID(),
        dedupKey = UUID.randomUUID().toString(),
        sourcePackage = "com.revolut.revolut",
        postedAt = now,
        amount = amount,
        currency = currency,
        direction = "OUT",
        payee = payee,
        categoryHint = hint,
        status = DetectedPaymentStatus.NEW,
        transactionId = null,
        createdAt = now,
    )

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

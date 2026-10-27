package com.spgrvl.kissos.domain.receipt

import arrow.core.Either
import com.spgrvl.kissos.base.TestDispatchersProvider
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.fake.FakeMerchantDao
import com.spgrvl.kissos.data.db.dao.fake.FakeReceiptDao
import com.spgrvl.kissos.data.db.dao.fake.FakeTransactionDao
import com.spgrvl.kissos.data.db.entity.TransactionEntity
import com.spgrvl.kissos.data.model.Afm
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.model.Merchant
import com.spgrvl.kissos.data.model.ReceiptStatus
import com.spgrvl.kissos.data.model.TransactionId
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.data.receipt.AadeDataSource
import com.spgrvl.kissos.data.receipt.AadeLookup
import com.spgrvl.kissos.data.receipt.AadeQr
import com.spgrvl.kissos.data.receipt.AadeReceipt
import com.spgrvl.kissos.data.receipt.ReceiptCode
import com.spgrvl.kissos.data.receipt.toPendingReceipt
import com.spgrvl.kissos.data.repository.MerchantRepository
import com.spgrvl.kissos.data.repository.ReceiptRepository
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class CheckPendingReceiptsUseCaseTest {
    private val now = Instant.parse("2026-10-22T18:00:00Z")
    private val qr = AadeQr.parse(
        "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DNX2500229500000595FB606FFCE12C6B8A38B439DE864D17261022185912.00"
    )!!
    private val found = AadeLookup.Found(
        AadeReceipt(
            registerId = "DNX25002295",
            receiptNumber = 595,
            mark = null,
            total = BigDecimal("12.00"),
            cardAmount = BigDecimal("0.00"),
            issuedAt = LocalDateTime.parse("2026-10-22T18:59"),
            issuedOn = LocalDate.parse("2026-10-22"),
            merchantAfm = "067022059",
            merchantName = "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ",
            address = null,
            documentType = null,
            netByVatCategory = mapOf('Ε' to BigDecimal("12.00")),
            vatByVatCategory = emptyMap(),
        )
    )

    private val transactionDao = FakeTransactionDao()
    private val receiptDao = FakeReceiptDao()
    private val merchantDao = FakeMerchantDao()
    private val aade = mockk<AadeDataSource>()
    private val receiptRepository = ReceiptRepository(receiptDao, receiptDao, TestDispatchersProvider)
    private val merchantRepository = MerchantRepository(merchantDao, merchantDao, TestDispatchersProvider)
    private val useCase = CheckPendingReceiptsUseCase(
        receiptRepository = receiptRepository,
        merchantRepository = merchantRepository,
        aadeDataSource = aade,
        transactionDao = transactionDao,
        writeTransactionDao = transactionDao,
        timeProvider = mockk<TimeProvider> { every { utcNow() } returns now },
    )

    private suspend fun givenPendingExpense(title: String? = null, scannedAt: Instant = now): TransactionId {
        val id = UUID.randomUUID()
        transactionDao.save(
            TransactionEntity(
                accountId = UUID.randomUUID(),
                type = TransactionType.EXPENSE,
                amount = 12.0,
                title = title,
                dateTime = now,
                id = id,
            )
        )
        receiptRepository.save(qr.toPendingReceipt(TransactionId(id), scannedAt))
        return TransactionId(id)
    }

    @Test
    fun `found - fills the empty title and category from merchant memory`() = runTest {
        // given
        val id = givenPendingExpense()
        val categoryId = UUID.randomUUID()
        merchantRepository.save(
            Merchant(
                afm = Afm.unsafe("067022059"),
                displayName = NotBlankTrimmedString.unsafe("Barber"),
                defaultCategory = CategoryId(categoryId),
                defaultAccount = null,
                updatedAt = now,
            )
        )
        coEvery { aade.lookup(any()) } returns Either.Right(found)

        // when
        val checkAgain = useCase.check()

        // then
        checkAgain shouldBe false
        receiptRepository.findByTransactionId(id)?.status shouldBe ReceiptStatus.FOUND
        val transaction = transactionDao.findById(id.value)!!
        transaction.title shouldBe "Barber"
        transaction.categoryId shouldBe categoryId
        transaction.amount shouldBe 12.0
    }

    @Test
    fun `found - keeps what the user already typed`() = runTest {
        // given
        val id = givenPendingExpense(title = "Haircut")
        coEvery { aade.lookup(any()) } returns Either.Right(found)

        // when
        useCase.check()

        // then
        transactionDao.findById(id.value)?.title shouldBe "Haircut"
    }

    @Test
    fun `a myDATA receipt is looked up with its link`() = runTest {
        // given
        val myData = ReceiptCode.MyData("https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo?q=abc")
        val id = UUID.randomUUID()
        receiptRepository.save(myData.toPendingReceipt(TransactionId(id), now))
        coEvery { aade.lookup(myData) } returns Either.Right(found)

        // when
        useCase.check()

        // then
        receiptRepository.findByTransactionId(TransactionId(id))?.status shouldBe ReceiptStatus.FOUND
    }

    @Test
    fun `offline - checks again later`() = runTest {
        // given
        val id = givenPendingExpense()
        coEvery { aade.lookup(any()) } returns Either.Left("Unable to resolve host")

        // when
        val checkAgain = useCase.check()

        // then
        checkAgain shouldBe true
        receiptRepository.findByTransactionId(id)?.status shouldBe ReceiptStatus.PENDING
    }

    @Test
    fun `not yet transmitted - checks again later and names the merchant`() = runTest {
        // given
        val id = givenPendingExpense()
        coEvery { aade.lookup(any()) } returns Either.Right(
            AadeLookup.NotYetTransmitted("DNX25002295", "067022059", "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ", null)
        )

        // when
        val checkAgain = useCase.check()

        // then
        checkAgain shouldBe true
        transactionDao.findById(id.value)?.title shouldBe "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ"
    }

    @Test
    fun `gives up on receipts scanned more than 3 days ago`() = runTest {
        // given
        givenPendingExpense(scannedAt = now.minus(Duration.ofDays(4)))

        // when
        val checkAgain = useCase.check()

        // then
        checkAgain shouldBe false
    }
}

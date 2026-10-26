package com.ivy.data.repository

import com.ivy.base.TestDispatchersProvider
import com.ivy.data.db.dao.fake.FakeMerchantDao
import com.ivy.data.db.dao.fake.FakeReceiptDao
import com.ivy.data.model.AccountId
import com.ivy.data.model.Afm
import com.ivy.data.model.Merchant
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.model.TransactionId
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.receipt.AadePage
import com.ivy.data.receipt.AadeQr
import com.ivy.data.receipt.AadeTestReceipt
import com.ivy.data.receipt.aadePage
import com.ivy.data.receipt.toPendingReceipt
import com.ivy.data.receipt.withLookup
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ReceiptRepositoryTest {
    private val receiptRepository = FakeReceiptDao().let {
        ReceiptRepository(receiptDao = it, writeReceiptDao = it, dispatchers = TestDispatchersProvider)
    }
    private val merchantRepository = FakeMerchantDao().let {
        MerchantRepository(merchantDao = it, writeMerchantDao = it, dispatchers = TestDispatchersProvider)
    }

    @Test
    fun `a fetched receipt is stored and read back unchanged`() = runTest {
        // given
        val receipt = AadeQr.parse(AadeTestReceipt.GRILL.qr)!!
            .toPendingReceipt(TransactionId(UUID.randomUUID()), Instant.parse("2026-10-17T18:00:00Z"))
            .withLookup(AadePage.parse(aadePage("grill")), Instant.parse("2026-10-17T18:00:05Z"))

        // when
        receiptRepository.save(receipt)

        // then
        receiptRepository.findByTransactionId(receipt.transactionId) shouldBe receipt
        receiptRepository.findById(receipt.id) shouldBe receipt
        receiptRepository.findPending() shouldBe emptyList()
    }

    @Test
    fun `pending receipts are listed for retrying`() = runTest {
        // given
        val receipt = AadeQr.parse(AadeTestReceipt.SUPERMARKET.qr)!!
            .toPendingReceipt(TransactionId(UUID.randomUUID()), Instant.parse("2026-10-22T08:20:00Z"))

        // when
        receiptRepository.save(receipt)

        // then
        receiptRepository.findPending().map { it.status } shouldBe listOf(ReceiptStatus.PENDING)
    }

    @Test
    fun `merchants are stored by tax id`() = runTest {
        // given
        val merchant = Merchant(
            afm = Afm.unsafe("044136828"),
            displayName = NotBlankTrimmedString.unsafe("Ψησταριά Παναγόπουλος"),
            defaultCategory = null,
            defaultAccount = AccountId(UUID.randomUUID()),
            updatedAt = Instant.parse("2026-10-17T18:00:00Z"),
        )

        // when
        merchantRepository.save(merchant)

        // then
        merchantRepository.findByAfm(merchant.afm) shouldBe merchant
        merchantRepository.deleteByAfm(merchant.afm)
        merchantRepository.findAll() shouldBe emptyList()
    }
}

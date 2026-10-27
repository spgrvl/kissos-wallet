package com.spgrvl.kissos.domain.receipt

import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.read.TransactionDao
import com.spgrvl.kissos.data.db.dao.write.WriteTransactionDao
import com.spgrvl.kissos.data.model.Receipt
import com.spgrvl.kissos.data.model.ReceiptStatus
import com.spgrvl.kissos.data.receipt.AadeDataSource
import com.spgrvl.kissos.data.receipt.ReceiptCode
import com.spgrvl.kissos.data.receipt.withLookup
import com.spgrvl.kissos.data.repository.MerchantRepository
import com.spgrvl.kissos.data.repository.ReceiptRepository
import java.time.Duration
import javax.inject.Inject

/**
 * Looks up the receipts that AADE didn't have yet (or that were scanned offline)
 * and fills in what's still empty on their transactions. Never changes the amount.
 */
class CheckPendingReceiptsUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val merchantRepository: MerchantRepository,
    private val aadeDataSource: AadeDataSource,
    private val transactionDao: TransactionDao,
    private val writeTransactionDao: WriteTransactionDao,
    private val timeProvider: TimeProvider,
) {
    companion object {
        /** Registers upload within a day; after this, stop asking. */
        val GIVE_UP_AFTER: Duration = Duration.ofDays(3)
    }

    /** @return true if some receipts are still pending and worth checking again later. */
    suspend fun check(): Boolean {
        val now = timeProvider.utcNow()
        var checkAgain = false
        receiptRepository.findPending()
            .filter { it.scannedAt.plus(GIVE_UP_AFTER).isAfter(now) }
            .forEach { receipt ->
                val code = ReceiptCode.fromKey(receipt.sig) ?: return@forEach
                aadeDataSource.lookup(code).fold(
                    ifLeft = { checkAgain = true },
                    ifRight = { lookup ->
                        val updated = receipt.withLookup(lookup, checkedAt = now)
                        receiptRepository.save(updated)
                        fillEmptyFields(updated)
                        if (updated.status == ReceiptStatus.PENDING) checkAgain = true
                    }
                )
            }
        return checkAgain
    }

    private suspend fun fillEmptyFields(receipt: Receipt) {
        val afm = receipt.merchantAfm ?: return
        val transaction = transactionDao.findById(receipt.transactionId.value) ?: return
        val merchant = merchantRepository.findByAfm(afm)
        val filled = transaction.copy(
            title = transaction.title?.takeIf { it.isNotBlank() }
                ?: merchant?.displayName?.value
                ?: receipt.merchantName,
            categoryId = transaction.categoryId ?: merchant?.defaultCategory?.value,
        )
        if (filled != transaction) {
            writeTransactionDao.save(filled)
        }
    }
}

package com.ivy.domain.receipt

import com.ivy.base.time.TimeProvider
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.db.dao.write.WriteTransactionDao
import com.ivy.data.model.Receipt
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.receipt.AadeDataSource
import com.ivy.data.receipt.AadeQr
import com.ivy.data.receipt.withLookup
import com.ivy.data.repository.MerchantRepository
import com.ivy.data.repository.ReceiptRepository
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
                val qr = AadeQr.parse(receipt.sig) ?: return@forEach
                aadeDataSource.lookup(qr).fold(
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

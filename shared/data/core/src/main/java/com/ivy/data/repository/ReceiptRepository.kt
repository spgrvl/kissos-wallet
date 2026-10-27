package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.ReceiptDao
import com.ivy.data.db.dao.write.WriteReceiptDao
import com.ivy.data.db.entity.ReceiptEntity
import com.ivy.data.model.Afm
import com.ivy.data.model.Receipt
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.model.TransactionId
import com.ivy.data.model.VatAmount
import com.ivy.data.receipt.toReceiptMoney
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val writeReceiptDao: WriteReceiptDao,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun findByTransactionId(id: TransactionId): Receipt? = withContext(dispatchers.io) {
        receiptDao.findByTransactionId(id.value)?.toDomain()
    }

    suspend fun findBySig(sig: String): Receipt? = withContext(dispatchers.io) {
        receiptDao.findBySig(sig)?.toDomain()
    }

    suspend fun findByMark(mark: Long): Receipt? = withContext(dispatchers.io) {
        receiptDao.findByMark(mark)?.toDomain()
    }

    suspend fun findPending(): List<Receipt> = withContext(dispatchers.io) {
        receiptDao.findAllByStatus(ReceiptStatus.PENDING).map { it.toDomain() }
    }

    suspend fun save(receipt: Receipt) {
        withContext(dispatchers.io) {
            writeReceiptDao.save(receipt.toEntity())
        }
    }

    private fun ReceiptEntity.toDomain(): Receipt = Receipt(
        transactionId = TransactionId(transactionId),
        sig = sig,
        registerId = registerId,
        receiptNumber = receiptNumber,
        mark = mark,
        total = total?.toReceiptMoney(),
        issuedAt = issuedAt,
        merchantAfm = merchantAfm?.let { Afm.from(it).getOrNull() },
        merchantName = merchantName,
        address = address,
        documentType = documentType,
        cardAmount = cardAmount?.toReceiptMoney(),
        vat = listOfNotNull(
            vatAmount('Α', netA, vatA),
            vatAmount('Β', netB, vatB),
            vatAmount('Γ', netC, vatC),
            vatAmount('Δ', netD, vatD),
            vatAmount('Ε', netE, vatE),
        ).toMap(),
        status = status,
        aadeMessage = aadeMessage,
        scannedAt = scannedAt,
        lastCheckedAt = lastCheckedAt,
    )

    private fun vatAmount(category: Char, net: Double?, vat: Double?): Pair<Char, VatAmount>? =
        if (net == null && vat == null) {
            null
        } else {
            category to VatAmount(
                net = (net ?: 0.0).toReceiptMoney(),
                vat = (vat ?: 0.0).toReceiptMoney(),
            )
        }

    private fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
        transactionId = transactionId.value,
        sig = sig,
        registerId = registerId,
        receiptNumber = receiptNumber,
        mark = mark,
        total = total?.toDouble(),
        issuedAt = issuedAt,
        merchantAfm = merchantAfm?.value,
        merchantName = merchantName,
        address = address,
        documentType = documentType,
        cardAmount = cardAmount?.toDouble(),
        netA = vat['Α']?.net?.toDouble(),
        vatA = vat['Α']?.vat?.toDouble(),
        netB = vat['Β']?.net?.toDouble(),
        vatB = vat['Β']?.vat?.toDouble(),
        netC = vat['Γ']?.net?.toDouble(),
        vatC = vat['Γ']?.vat?.toDouble(),
        netD = vat['Δ']?.net?.toDouble(),
        vatD = vat['Δ']?.vat?.toDouble(),
        netE = vat['Ε']?.net?.toDouble(),
        vatE = vat['Ε']?.vat?.toDouble(),
        status = status,
        aadeMessage = aadeMessage,
        scannedAt = scannedAt,
        lastCheckedAt = lastCheckedAt,
    )
}

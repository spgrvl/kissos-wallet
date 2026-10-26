package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.ReceiptDao
import com.ivy.data.db.dao.write.WriteReceiptDao
import com.ivy.data.db.entity.ReceiptEntity
import com.ivy.data.model.ReceiptStatus
import org.jetbrains.annotations.VisibleForTesting
import java.util.UUID

@VisibleForTesting
class FakeReceiptDao : ReceiptDao, WriteReceiptDao {
    private val items = mutableMapOf<UUID, ReceiptEntity>()

    override suspend fun findAll(): List<ReceiptEntity> = items.values.toList()

    override suspend fun findByTransactionId(transactionId: UUID): ReceiptEntity? = items[transactionId]

    override suspend fun findByRegisterAndNumber(registerId: String, receiptNumber: Long): ReceiptEntity? =
        items.values.find { it.registerId == registerId && it.receiptNumber == receiptNumber }

    override suspend fun findAllByStatus(status: ReceiptStatus): List<ReceiptEntity> =
        items.values.filter { it.status == status }

    override suspend fun save(value: ReceiptEntity) {
        items[value.transactionId] = value
    }

    override suspend fun saveMany(values: List<ReceiptEntity>) {
        values.forEach { save(it) }
    }

    override suspend fun deleteByTransactionId(transactionId: UUID) {
        items.remove(transactionId)
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}

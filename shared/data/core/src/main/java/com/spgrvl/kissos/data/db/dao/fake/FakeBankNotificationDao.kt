package com.spgrvl.kissos.data.db.dao.fake

import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.CategoryHintMemoryEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.PayeeMemoryEntity
import com.spgrvl.kissos.data.db.entity.UnparsedNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.VisibleForTesting
import java.time.Instant
import java.util.UUID

@VisibleForTesting
@Suppress("TooManyFunctions")
class FakeBankNotificationDao : BankNotificationDao {
    private val payments = MutableStateFlow<Map<UUID, DetectedPaymentEntity>>(emptyMap())
    private val payees = mutableMapOf<String, PayeeMemoryEntity>()
    private val hints = mutableMapOf<Pair<String, String>, CategoryHintMemoryEntity>()
    private val unparsed = MutableStateFlow<List<UnparsedNotificationEntity>>(emptyList())

    override suspend fun insertPayment(value: DetectedPaymentEntity): Long =
        if (payments.value.values.any { it.dedupKey == value.dedupKey }) {
            -1L
        } else {
            payments.value += value.id to value
            payments.value.size.toLong()
        }

    override suspend fun findPayment(id: UUID): DetectedPaymentEntity? = payments.value[id]

    override suspend fun findPaymentByTransaction(transactionId: UUID): DetectedPaymentEntity? =
        payments.value.values.find { it.transactionId == transactionId }

    override suspend fun countPayments(status: DetectedPaymentStatus): Long =
        payments.value.values.count { it.status == status }.toLong()

    override fun observePayments(status: DetectedPaymentStatus): Flow<List<DetectedPaymentEntity>> =
        payments.map { all -> all.values.filter { it.status == status }.sortedByDescending { it.postedAt } }

    override fun observeAllPayments(): Flow<List<DetectedPaymentEntity>> =
        payments.map { all -> all.values.sortedByDescending { it.postedAt } }

    override suspend fun setStatus(id: UUID, status: DetectedPaymentStatus, transactionId: UUID?) {
        payments.value[id]?.let { payments.value += id to it.copy(status = status, transactionId = transactionId) }
    }

    override suspend fun deleteAllPayments() {
        payments.value = emptyMap()
    }

    override suspend fun deleteHandledBefore(before: Instant) {
        payments.value = payments.value.filterValues {
            it.status == DetectedPaymentStatus.NEW || !it.postedAt.isBefore(before)
        }
    }

    override suspend fun findPayee(payee: String): PayeeMemoryEntity? = payees[payee]

    override suspend fun savePayee(value: PayeeMemoryEntity) {
        payees[value.payee] = value
    }

    override suspend fun savePayees(values: List<PayeeMemoryEntity>) {
        values.forEach { savePayee(it) }
    }

    override suspend fun findAllPayees(): List<PayeeMemoryEntity> = payees.values.toList()

    override suspend fun deleteAllPayees() {
        payees.clear()
    }

    override suspend fun findCategoryHint(sourcePackage: String, hint: String): CategoryHintMemoryEntity? =
        hints[sourcePackage to hint]

    override suspend fun saveCategoryHint(value: CategoryHintMemoryEntity) {
        hints[value.sourcePackage to value.hint] = value
    }

    override suspend fun saveCategoryHints(values: List<CategoryHintMemoryEntity>) {
        values.forEach { saveCategoryHint(it) }
    }

    override suspend fun findAllCategoryHints(): List<CategoryHintMemoryEntity> = hints.values.toList()

    override suspend fun deleteAllCategoryHints() {
        hints.clear()
    }

    override suspend fun insertUnparsed(value: UnparsedNotificationEntity) {
        unparsed.value += value
    }

    override fun observeUnparsed(): Flow<List<UnparsedNotificationEntity>> = unparsed

    override suspend fun deleteUnparsedBefore(before: Instant) {
        unparsed.value = unparsed.value.filterNot { it.postedAt.isBefore(before) }
    }

    override suspend fun deleteAllUnparsed() {
        unparsed.value = emptyList()
    }
}

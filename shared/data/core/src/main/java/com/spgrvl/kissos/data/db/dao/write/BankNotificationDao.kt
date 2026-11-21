package com.spgrvl.kissos.data.db.dao.write

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.spgrvl.kissos.data.db.entity.CategoryHintMemoryEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.PayeeMemoryEntity
import com.spgrvl.kissos.data.db.entity.UnparsedNotificationEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

/** Payments detected in bank apps' notifications, and what Kissos learned from the user about them. */
@Dao
interface BankNotificationDao {
    /** @return -1 if a payment with the same `dedupKey` was already detected. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayment(value: DetectedPaymentEntity): Long

    @Query("SELECT * FROM detected_payments WHERE id = :id")
    suspend fun findPayment(id: UUID): DetectedPaymentEntity?

    @Query("SELECT * FROM detected_payments WHERE transactionId = :transactionId")
    suspend fun findPaymentByTransaction(transactionId: UUID): DetectedPaymentEntity?

    @Query("SELECT COUNT(*) FROM detected_payments WHERE status = :status")
    suspend fun countPayments(status: DetectedPaymentStatus): Long

    @Query("SELECT * FROM detected_payments WHERE status = :status ORDER BY postedAt DESC")
    fun observePayments(status: DetectedPaymentStatus): Flow<List<DetectedPaymentEntity>>

    @Query("SELECT * FROM detected_payments ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecentPayments(limit: Int): Flow<List<DetectedPaymentEntity>>

    @Query("UPDATE detected_payments SET status = :status, transactionId = :transactionId WHERE id = :id")
    suspend fun setStatus(id: UUID, status: DetectedPaymentStatus, transactionId: UUID?)

    @Query("DELETE FROM detected_payments")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM detected_payments WHERE status != 'NEW' AND postedAt < :before")
    suspend fun deleteHandledBefore(before: Instant)

    @Query("SELECT * FROM payee_memory WHERE payee = :payee")
    suspend fun findPayee(payee: String): PayeeMemoryEntity?

    @Upsert
    suspend fun savePayee(value: PayeeMemoryEntity)

    @Upsert
    suspend fun savePayees(values: List<PayeeMemoryEntity>)

    @Query("SELECT * FROM payee_memory")
    suspend fun findAllPayees(): List<PayeeMemoryEntity>

    @Query("DELETE FROM payee_memory")
    suspend fun deleteAllPayees()

    @Query("SELECT * FROM category_hint_memory WHERE sourcePackage = :sourcePackage AND hint = :hint")
    suspend fun findCategoryHint(sourcePackage: String, hint: String): CategoryHintMemoryEntity?

    @Upsert
    suspend fun saveCategoryHint(value: CategoryHintMemoryEntity)

    @Upsert
    suspend fun saveCategoryHints(values: List<CategoryHintMemoryEntity>)

    @Query("SELECT * FROM category_hint_memory")
    suspend fun findAllCategoryHints(): List<CategoryHintMemoryEntity>

    @Query("DELETE FROM category_hint_memory")
    suspend fun deleteAllCategoryHints()

    @Insert
    suspend fun insertUnparsed(value: UnparsedNotificationEntity)

    @Query("SELECT * FROM unparsed_notifications ORDER BY postedAt DESC")
    fun observeUnparsed(): Flow<List<UnparsedNotificationEntity>>

    @Query("DELETE FROM unparsed_notifications WHERE postedAt < :before")
    suspend fun deleteUnparsedBefore(before: Instant)

    @Query("DELETE FROM unparsed_notifications")
    suspend fun deleteAllUnparsed()
}

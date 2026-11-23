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

    /** All of them, newest first: handled ones are purged after 30 days, so there are never many. */
    @Query("SELECT * FROM detected_payments ORDER BY postedAt DESC")
    fun observeAllPayments(): Flow<List<DetectedPaymentEntity>>

    @Query("UPDATE detected_payments SET status = :status, transactionId = :transactionId WHERE id = :id")
    suspend fun setStatus(id: UUID, status: DetectedPaymentStatus, transactionId: UUID?)

    /**
     * The earliest payment from [sourcePackage] of this amount between [from] and [to] that no account debit is
     * matched to yet, whatever the user did with it.
     */
    @Query(
        "SELECT * FROM detected_payments WHERE sourcePackage = :sourcePackage AND kind = 'PAYMENT' " +
            "AND direction = :direction AND currency = :currency AND ABS(amount - :amount) < 0.005 " +
            "AND postedAt BETWEEN :from AND :to " +
            "AND id NOT IN (SELECT matchedPaymentId FROM detected_payments WHERE matchedPaymentId IS NOT NULL) " +
            "ORDER BY postedAt ASC LIMIT 1"
    )
    suspend fun findUnmatchedPayment(
        sourcePackage: String,
        direction: String,
        currency: String,
        amount: Double,
        from: Instant,
        to: Instant,
    ): DetectedPaymentEntity?

    /** The payments (not account debits) from [sourcePackage] between [from] and [to]. */
    @Query(
        "SELECT * FROM detected_payments WHERE sourcePackage = :sourcePackage AND kind = 'PAYMENT' " +
            "AND postedAt BETWEEN :from AND :to"
    )
    suspend fun findPayments(sourcePackage: String, from: Instant, to: Instant): List<DetectedPaymentEntity>

    /** Back to waiting for the user, no longer matched to another payment. */
    @Query("UPDATE detected_payments SET status = 'NEW', transactionId = NULL, matchedPaymentId = NULL WHERE id = :id")
    suspend fun unmatch(id: UUID)

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

    /** Whether it's kept already, e.g. read again from the notifications still shown after reconnecting. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM unparsed_notifications " +
            "WHERE sourcePackage = :sourcePackage AND postedAt = :postedAt AND text = :text)"
    )
    suspend fun hasUnparsed(sourcePackage: String, postedAt: Instant, text: String): Boolean

    @Query("SELECT * FROM unparsed_notifications ORDER BY postedAt DESC")
    fun observeUnparsed(): Flow<List<UnparsedNotificationEntity>>

    @Query("DELETE FROM unparsed_notifications WHERE postedAt < :before")
    suspend fun deleteUnparsedBefore(before: Instant)

    @Query("DELETE FROM unparsed_notifications")
    suspend fun deleteAllUnparsed()
}

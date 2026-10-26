package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.ReceiptEntity
import com.ivy.data.model.ReceiptStatus
import java.util.UUID

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts")
    suspend fun findAll(): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE transactionId = :transactionId")
    suspend fun findByTransactionId(transactionId: UUID): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE registerId = :registerId AND receiptNumber = :receiptNumber")
    suspend fun findByRegisterAndNumber(registerId: String, receiptNumber: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE status = :status")
    suspend fun findAllByStatus(status: ReceiptStatus): List<ReceiptEntity>
}

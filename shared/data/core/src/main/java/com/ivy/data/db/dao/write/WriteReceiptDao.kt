package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.ReceiptEntity
import java.util.UUID

@Dao
interface WriteReceiptDao {
    @Upsert
    suspend fun save(value: ReceiptEntity)

    @Upsert
    suspend fun saveMany(values: List<ReceiptEntity>)

    @Query("DELETE FROM receipts WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: UUID)

    @Query("DELETE FROM receipts")
    suspend fun deleteAll()
}

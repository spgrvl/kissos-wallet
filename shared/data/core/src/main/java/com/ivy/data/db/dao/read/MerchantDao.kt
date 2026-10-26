package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.MerchantEntity

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants ORDER BY displayName COLLATE NOCASE")
    suspend fun findAll(): List<MerchantEntity>

    @Query("SELECT * FROM merchants WHERE afm = :afm")
    suspend fun findByAfm(afm: String): MerchantEntity?
}

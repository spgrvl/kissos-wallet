package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.MerchantEntity

@Dao
interface WriteMerchantDao {
    @Upsert
    suspend fun save(value: MerchantEntity)

    @Upsert
    suspend fun saveMany(values: List<MerchantEntity>)

    @Query("DELETE FROM merchants WHERE afm = :afm")
    suspend fun deleteByAfm(afm: String)

    @Query("DELETE FROM merchants")
    suspend fun deleteAll()
}

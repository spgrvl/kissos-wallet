package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.MerchantDao
import com.ivy.data.db.dao.write.WriteMerchantDao
import com.ivy.data.db.entity.MerchantEntity
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
class FakeMerchantDao : MerchantDao, WriteMerchantDao {
    private val items = mutableMapOf<String, MerchantEntity>()

    override suspend fun findAll(): List<MerchantEntity> =
        items.values.sortedBy { it.displayName.lowercase() }

    override suspend fun findByAfm(afm: String): MerchantEntity? = items[afm]

    override suspend fun save(value: MerchantEntity) {
        items[value.afm] = value
    }

    override suspend fun saveMany(values: List<MerchantEntity>) {
        values.forEach { save(it) }
    }

    override suspend fun deleteByAfm(afm: String) {
        items.remove(afm)
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}

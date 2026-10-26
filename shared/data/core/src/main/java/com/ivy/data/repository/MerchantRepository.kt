package com.ivy.data.repository

import arrow.core.raise.either
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.MerchantDao
import com.ivy.data.db.dao.write.WriteMerchantDao
import com.ivy.data.db.entity.MerchantEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.Afm
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Merchant
import com.ivy.data.model.primitive.NotBlankTrimmedString
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MerchantRepository @Inject constructor(
    private val merchantDao: MerchantDao,
    private val writeMerchantDao: WriteMerchantDao,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun findAll(): List<Merchant> = withContext(dispatchers.io) {
        merchantDao.findAll().mapNotNull { it.toDomain() }
    }

    suspend fun findByAfm(afm: Afm): Merchant? = withContext(dispatchers.io) {
        merchantDao.findByAfm(afm.value)?.toDomain()
    }

    suspend fun save(merchant: Merchant) {
        withContext(dispatchers.io) {
            writeMerchantDao.save(merchant.toEntity())
        }
    }

    suspend fun deleteByAfm(afm: Afm) {
        withContext(dispatchers.io) {
            writeMerchantDao.deleteByAfm(afm.value)
        }
    }

    private fun MerchantEntity.toDomain(): Merchant? = either {
        Merchant(
            afm = Afm.from(afm).bind(),
            displayName = NotBlankTrimmedString.from(displayName).bind(),
            defaultCategory = defaultCategoryId?.let(::CategoryId),
            defaultAccount = defaultAccountId?.let(::AccountId),
            updatedAt = updatedAt,
        )
    }.getOrNull()

    private fun Merchant.toEntity(): MerchantEntity = MerchantEntity(
        afm = afm.value,
        displayName = displayName.value,
        defaultCategoryId = defaultCategory?.value,
        defaultAccountId = defaultAccount?.value,
        updatedAt = updatedAt,
    )
}

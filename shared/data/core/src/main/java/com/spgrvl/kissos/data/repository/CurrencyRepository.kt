package com.spgrvl.kissos.data.repository

import android.icu.util.Currency
import com.spgrvl.kissos.base.threading.DispatchersProvider
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.data.model.primitive.AssetCode
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    companion object {
        const val FALLBACK_DEFAULT_CURRENCY = "USD"
    }

    private var baseCurrencyMemo: AssetCode? = null

    suspend fun getBaseCurrency(): AssetCode = withContext(dispatchersProvider.io) {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return@withContext baseCurrency

        val currencyCode = settingsDao.findFirstOrNull()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(FALLBACK_DEFAULT_CURRENCY)
    }

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }
}
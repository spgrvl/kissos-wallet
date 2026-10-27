package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.ExchangeRateEntity
import com.spgrvl.kissos.legacy.datamodel.ExchangeRate

fun ExchangeRateEntity.toLegacyDomain(): ExchangeRate = ExchangeRate(
    baseCurrency = baseCurrency,
    currency = currency,
    rate = rate
)

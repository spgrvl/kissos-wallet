package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.SettingsEntity
import com.spgrvl.kissos.legacy.datamodel.Settings

fun SettingsEntity.toLegacyDomain(): Settings = Settings(
    theme = theme,
    baseCurrency = currency,
    bufferAmount = bufferAmount.toBigDecimal(),
    name = name,
    id = id
)

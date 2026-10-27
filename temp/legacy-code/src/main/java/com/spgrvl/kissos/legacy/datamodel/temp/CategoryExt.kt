package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.CategoryEntity
import com.spgrvl.kissos.legacy.datamodel.Category

fun CategoryEntity.toLegacyDomain(): Category = Category(
    name = name,
    color = color,
    icon = icon,
    orderNum = orderNum,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id
)

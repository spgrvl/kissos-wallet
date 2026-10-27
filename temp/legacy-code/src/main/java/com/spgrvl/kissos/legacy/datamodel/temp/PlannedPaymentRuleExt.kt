package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.PlannedPaymentRuleEntity
import com.spgrvl.kissos.legacy.datamodel.PlannedPaymentRule

fun PlannedPaymentRuleEntity.toLegacyDomain(): PlannedPaymentRule = PlannedPaymentRule(
    startDate = startDate,
    intervalN = intervalN,
    intervalType = intervalType,
    oneTime = oneTime,
    type = type,
    accountId = accountId,
    amount = amount,
    categoryId = categoryId,
    title = title,
    description = description,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id
)

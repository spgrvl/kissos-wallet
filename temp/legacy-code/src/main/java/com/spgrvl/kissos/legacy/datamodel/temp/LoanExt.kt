package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.LoanEntity
import com.spgrvl.kissos.data.model.LoanType
import com.spgrvl.kissos.legacy.datamodel.Loan

fun LoanEntity.toLegacyDomain(): Loan = Loan(
    name = name,
    amount = amount,
    type = type,
    color = color,
    icon = icon,
    orderNum = orderNum,
    accountId = accountId,
    note = note,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id,
    dateTime = dateTime
)

fun LoanEntity.humanReadableType(): String {
    return if (type == LoanType.BORROW) "BORROWED" else "LENT"
}

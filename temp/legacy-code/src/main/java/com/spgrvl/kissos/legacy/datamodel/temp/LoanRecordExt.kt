package com.spgrvl.kissos.legacy.datamodel.temp

import com.spgrvl.kissos.data.db.entity.LoanRecordEntity
import com.spgrvl.kissos.legacy.datamodel.LoanRecord

fun LoanRecordEntity.toLegacyDomain(): LoanRecord = LoanRecord(
    loanId = loanId,
    amount = amount,
    note = note,
    dateTime = dateTime,
    interest = interest,
    accountId = accountId,
    convertedAmount = convertedAmount,
    loanRecordType = loanRecordType,
    isSynced = isSynced,
    isDeleted = isDeleted,
    id = id
)

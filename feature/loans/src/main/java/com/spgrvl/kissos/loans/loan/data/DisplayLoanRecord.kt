package com.spgrvl.kissos.loans.loan.data

import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.legacy.datamodel.LoanRecord

data class DisplayLoanRecord(
    val loanRecord: LoanRecord,
    val account: Account? = null,
    val loanRecordCurrencyCode: String = "",
    val loanCurrencyCode: String = "",
    val loanRecordTransaction: Boolean = false,
)

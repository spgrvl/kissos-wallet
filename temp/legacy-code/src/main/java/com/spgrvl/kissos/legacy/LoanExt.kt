package com.spgrvl.kissos.legacy

import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.data.model.LoanType
import com.spgrvl.kissos.legacy.datamodel.Loan
import com.spgrvl.kissos.ui.R

fun Loan.humanReadableType(): String {
    return if (type == LoanType.BORROW) {
        stringRes(R.string.borrowed_uppercase)
    } else {
        stringRes(R.string.lent_uppercase)
    }
}

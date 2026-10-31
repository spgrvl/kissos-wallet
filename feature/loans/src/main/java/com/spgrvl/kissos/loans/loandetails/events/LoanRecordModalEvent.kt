package com.spgrvl.kissos.loans.loandetails.events

import com.spgrvl.kissos.legacy.datamodel.LoanRecord
import com.spgrvl.kissos.loans.loan.data.DisplayLoanRecord
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateLoanRecordData
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.EditLoanRecordData

sealed interface LoanRecordModalEvent : LoanDetailsScreenEvent {
    data class OnClickLoanRecord(val displayLoanRecord: DisplayLoanRecord) : LoanRecordModalEvent
    data class OnCreateLoanRecord(val loanRecordData: CreateLoanRecordData) :
        LoanRecordModalEvent

    data class OnDeleteLoanRecord(val loanRecord: LoanRecord) : LoanRecordModalEvent
    data class OnEditLoanRecord(val loanRecordData: EditLoanRecordData) : LoanRecordModalEvent
    data object OnDismissLoanRecord : LoanRecordModalEvent

    data object OnChangeDate : LoanRecordModalEvent
    data object OnChangeTime : LoanRecordModalEvent
}

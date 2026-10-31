package com.spgrvl.kissos.loans.loandetails.events

import com.spgrvl.kissos.legacy.datamodel.Loan

sealed interface LoanModalEvent : LoanDetailsScreenEvent {
    data object OnDismissLoanModal : LoanModalEvent
    data class OnEditLoanModal(val loan: Loan, val createLoanTransaction: Boolean) :
        LoanModalEvent

    data object PerformCalculation : LoanModalEvent
    data object OnChangeDate : LoanModalEvent
    data object OnChangeTime : LoanModalEvent
}

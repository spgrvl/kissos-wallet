package com.spgrvl.kissos.loans.loandetails.events

sealed interface DeleteLoanModalEvent : LoanDetailsScreenEvent {
    data object OnDeleteLoan : DeleteLoanModalEvent
    data class OnDismissDeleteLoan(val isDeleteModalVisible: Boolean) : DeleteLoanModalEvent
}

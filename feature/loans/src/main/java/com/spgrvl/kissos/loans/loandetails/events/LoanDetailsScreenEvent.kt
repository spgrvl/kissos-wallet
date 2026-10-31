package com.spgrvl.kissos.loans.loandetails.events

import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData

sealed interface LoanDetailsScreenEvent {
    data object OnEditLoanClick : LoanDetailsScreenEvent
    data object OnAmountClick : LoanDetailsScreenEvent
    data object OnAddRecord : LoanDetailsScreenEvent
    data class OnCreateAccount(val data: CreateAccountData) : LoanDetailsScreenEvent
}

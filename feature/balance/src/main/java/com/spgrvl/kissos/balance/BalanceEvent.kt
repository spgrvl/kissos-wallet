package com.spgrvl.kissos.balance

import com.spgrvl.kissos.legacy.data.model.TimePeriod

sealed interface BalanceEvent {
    data class OnSetPeriod(val timePeriod: TimePeriod) : BalanceEvent
    data object OnPreviousMonth : BalanceEvent
    data object OnNextMonth : BalanceEvent
}

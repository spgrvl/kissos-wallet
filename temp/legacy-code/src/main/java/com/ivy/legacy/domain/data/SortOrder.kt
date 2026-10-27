package com.ivy.wallet.domain.data

import androidx.annotation.StringRes
import com.ivy.ui.R

enum class SortOrder(val orderNum: Int, @StringRes val displayName: Int) {
    DEFAULT(0, R.string.sort_default),
    BALANCE_AMOUNT(1, R.string.sort_balance_amount),
    EXPENSES(2, R.string.expenses),
    ALPHABETICAL(3, R.string.sort_alphabetical);

    companion object {
        fun from(orderNum: Int): SortOrder {
            return values().firstOrNull { it.orderNum == orderNum } ?: DEFAULT
        }
    }
}

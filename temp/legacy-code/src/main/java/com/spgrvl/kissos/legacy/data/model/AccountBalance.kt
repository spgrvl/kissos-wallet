package com.spgrvl.kissos.legacy.data.model

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.legacy.datamodel.Account

@Immutable
data class AccountBalance(
    val account: Account,
    val balance: Double
)

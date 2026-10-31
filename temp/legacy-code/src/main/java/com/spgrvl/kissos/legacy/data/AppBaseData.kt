package com.spgrvl.kissos.legacy.data

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class AppBaseData(
    val baseCurrency: String,
    val accounts: ImmutableList<Account>,
    val categories: ImmutableList<Category>
)

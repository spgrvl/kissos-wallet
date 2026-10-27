package com.spgrvl.kissos.search

import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList

data class SearchState(
    val searchQuery: String,
    val transactions: ImmutableList<TransactionHistoryItem>,
    val baseCurrency: String,
    val accounts: ImmutableList<Account>,
    val categories: ImmutableList<Category>,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

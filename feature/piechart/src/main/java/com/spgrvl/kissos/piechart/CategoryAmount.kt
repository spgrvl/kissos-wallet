package com.spgrvl.kissos.piechart

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.data.model.Category

@Immutable
data class CategoryAmount(
    val category: Category?,
    val amount: Double,
    val associatedTransactions: List<Transaction> = emptyList(),
    val isCategoryUnspecified: Boolean = false
)

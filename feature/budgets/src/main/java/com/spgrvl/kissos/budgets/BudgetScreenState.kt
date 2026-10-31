package com.spgrvl.kissos.budgets

import com.spgrvl.kissos.budgets.model.DisplayBudget
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.legacy.data.model.FromToTimeRange
import com.spgrvl.kissos.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

@Immutable
data class BudgetScreenState(
    val baseCurrency: String,
    val budgets: ImmutableList<DisplayBudget>,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<Account>,
    val categoryBudgetsTotal: Double,
    val appBudgetMax: Double,
    val totalRemainingBudgetText: String?,
    val timeRange: FromToTimeRange?,
    val reorderModalVisible: Boolean,
    val budgetModalData: BudgetModalData?
)

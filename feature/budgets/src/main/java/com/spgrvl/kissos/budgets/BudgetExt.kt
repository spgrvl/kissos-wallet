package com.spgrvl.kissos.budgets

import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.ui.R

fun determineBudgetType(categoriesCount: Int): String {
    return when (categoriesCount) {
        0 -> stringRes(R.string.total_budget)
        1 -> stringRes(R.string.category_budget)
        else -> stringRes(R.string.multi_category_budget, categoriesCount.toString())
    }
}

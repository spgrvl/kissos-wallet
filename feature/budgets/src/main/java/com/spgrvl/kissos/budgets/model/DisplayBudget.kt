package com.spgrvl.kissos.budgets.model

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.legacy.datamodel.Budget
import com.spgrvl.kissos.wallet.domain.data.Reorderable

@Immutable
data class DisplayBudget(
    val budget: Budget,
    val spentAmount: Double
) : Reorderable {
    override fun getItemOrderNum(): Double {
        return budget.orderId
    }

    override fun withNewOrderNum(newOrderNum: Double): Reorderable {
        return this.copy(
            budget = budget.copy(
                orderId = newOrderNum
            )
        )
    }
}

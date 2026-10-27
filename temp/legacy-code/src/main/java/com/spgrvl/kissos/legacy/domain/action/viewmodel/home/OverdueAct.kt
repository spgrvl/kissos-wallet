package com.spgrvl.kissos.wallet.domain.action.viewmodel.home

import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.utils.kissosMinTime
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpensePair
import com.spgrvl.kissos.wallet.domain.pure.transaction.isOverdue
import java.time.Instant
import javax.inject.Inject

class OverdueAct @Inject constructor(
    private val dueTrnsInfoAct: DueTrnsInfoAct
) : FPAction<OverdueAct.Input, OverdueAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
        DueTrnsInfoAct.Input(
            range = ClosedTimeRange(
                from = kissosMinTime(),
                to = toRange
            ),
            baseCurrency = baseCurrency,
            dueFilter = ::isOverdue
        )
    } then dueTrnsInfoAct then {
        Output(
            overdue = it.dueIncomeExpense,
            overdueTrns = it.dueTrns
        )
    }

    data class Input(
        val toRange: Instant,
        val baseCurrency: String
    )

    data class Output(
        val overdue: IncomeExpensePair,
        val overdueTrns: List<com.spgrvl.kissos.data.model.Transaction>
    )
}

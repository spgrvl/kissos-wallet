package com.spgrvl.kissos.wallet.domain.action.account

import arrow.core.nonEmptyListOf
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpensePair
import com.spgrvl.kissos.legacy.domain.pure.transaction.AccountValueFunctions
import com.spgrvl.kissos.wallet.domain.pure.transaction.foldTransactions
import java.math.BigDecimal
import javax.inject.Inject

class CalcAccIncomeExpenseAct @Inject constructor(
    private val accTrnsAct: AccTrnsAct,
    private val timeProvider: TimeProvider
) : FPAction<CalcAccIncomeExpenseAct.Input, CalcAccIncomeExpenseAct.Output>() {

    override suspend fun Input.compose(): suspend () -> Output = suspend {
        AccTrnsAct.Input(
            accountId = account.id.value,
            range = range ?: ClosedTimeRange.allTimeIvy(timeProvider)
        )
    } then accTrnsAct then { accTrns ->
        foldTransactions(
            transactions = accTrns,
            arg = account.id.value,
            valueFunctions = nonEmptyListOf(
                AccountValueFunctions::income,
                AccountValueFunctions::expense,
                AccountValueFunctions::transferIncome,
                AccountValueFunctions::transferExpense
            )
        )
    } then { values ->
        Output(
            account = account,
            incomeExpensePair = IncomeExpensePair(
                income = values[0] + if (includeTransfersInCalc) values[2] else BigDecimal.ZERO,
                expense = values[1] + if (includeTransfersInCalc) values[3] else BigDecimal.ZERO
            )
        )
    }

    @Suppress("DataClassDefaultValues")
    data class Input(
        val account: com.spgrvl.kissos.data.model.Account,
        val range: ClosedTimeRange? = null,
        val includeTransfersInCalc: Boolean = false
    )

    data class Output(
        val account: com.spgrvl.kissos.data.model.Account,
        val incomeExpensePair: IncomeExpensePair
    )
}

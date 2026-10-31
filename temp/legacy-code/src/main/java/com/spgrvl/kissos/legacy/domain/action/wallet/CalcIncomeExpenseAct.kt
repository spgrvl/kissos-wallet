package com.spgrvl.kissos.wallet.domain.action.wallet

import arrow.core.nonEmptyListOf
import arrow.core.toOption
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenMap
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.action.account.AccTrnsAct
import com.spgrvl.kissos.wallet.domain.action.exchange.ExchangeAct
import com.spgrvl.kissos.wallet.domain.pure.account.filterExcluded
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpensePair
import com.spgrvl.kissos.wallet.domain.pure.exchange.ExchangeData
import com.spgrvl.kissos.legacy.domain.pure.transaction.AccountValueFunctions
import com.spgrvl.kissos.wallet.domain.pure.transaction.foldTransactions
import com.spgrvl.kissos.wallet.domain.pure.util.orZero
import timber.log.Timber
import javax.inject.Inject

class CalcIncomeExpenseAct @Inject constructor(
    private val accTrnsAct: AccTrnsAct,
    private val exchangeAct: ExchangeAct
) : FPAction<CalcIncomeExpenseAct.Input, IncomeExpensePair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpensePair = suspend {
        filterExcluded(accounts)
    } thenMap { acc ->
        Pair(
            acc,
            accTrnsAct(
                AccTrnsAct.Input(
                    accountId = acc.id,
                    range = range
                )
            )
        )
    } thenMap { (acc, trns) ->
        Timber.i("acc: $acc, trns = ${trns.size}")
        Pair(
            acc,
            foldTransactions(
                transactions = trns,
                valueFunctions = nonEmptyListOf(
                    AccountValueFunctions::income,
                    AccountValueFunctions::expense
                ),
                arg = acc.id
            )
        )
    } thenMap { (acc, stats) ->
        Timber.i("acc_stats: $acc - $stats")
        stats.map {
            exchangeAct(
                ExchangeAct.Input(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = (acc.currency ?: baseCurrency).toOption()
                    ),
                    amount = it
                ),
            ).orZero()
        }
    } then { statsList ->
        IncomeExpensePair(
            income = statsList.sumOf { it[0] },
            expense = statsList.sumOf { it[1] }
        )
    }

    data class Input(
        val baseCurrency: String,
        val accounts: List<Account>,
        val range: ClosedTimeRange,
    )
}

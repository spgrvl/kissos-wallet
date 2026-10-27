package com.spgrvl.kissos.wallet.domain.action.transaction

import arrow.core.nonEmptyListOf
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.action.exchange.ExchangeAct
import com.spgrvl.kissos.wallet.domain.action.exchange.actInput
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpenseTransferPair
import com.spgrvl.kissos.wallet.domain.pure.transaction.LegacyFoldTransactions
import com.spgrvl.kissos.wallet.domain.pure.transaction.WalletValueFunctions
import com.spgrvl.kissos.wallet.domain.pure.transaction.WalletValueFunctionsLegacy
import com.spgrvl.kissos.wallet.domain.pure.transaction.foldTransactionsSuspend
import javax.inject.Inject

class CalcTrnsIncomeExpenseAct @Inject constructor(
    private val exchangeAct: ExchangeAct
) : FPAction<CalcTrnsIncomeExpenseAct.Input, IncomeExpenseTransferPair>() {
    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        foldTransactionsSuspend(
            transactions = transactions,
            valueFunctions = nonEmptyListOf(
                WalletValueFunctions::income,
                WalletValueFunctions::expense,
                WalletValueFunctions::transferIncome,
                WalletValueFunctions::transferExpenses
            ),
            arg = WalletValueFunctions.Argument(
                accounts = accounts,
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct
            )
        )
    } then { values ->
        IncomeExpenseTransferPair(
            income = values[0],
            expense = values[1],
            transferIncome = values[2],
            transferExpense = values[3]
        )
    }

    data class Input(
        val transactions: List<Transaction>,
        val baseCurrency: String,
        val accounts: List<Account>
    )
}

@Deprecated("Uses legacy Transaction")
class LegacyCalcTrnsIncomeExpenseAct @Inject constructor(
    private val exchangeAct: ExchangeAct
) : FPAction<LegacyCalcTrnsIncomeExpenseAct.Input, IncomeExpenseTransferPair>() {
    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        LegacyFoldTransactions.foldTransactionsSuspend(
            transactions = transactions,
            valueFunctions = nonEmptyListOf(
                WalletValueFunctionsLegacy::income,
                WalletValueFunctionsLegacy::expense,
                WalletValueFunctionsLegacy::transferIncome,
                WalletValueFunctionsLegacy::transferExpenses
            ),
            arg = WalletValueFunctionsLegacy.Argument(
                accounts = accounts,
                baseCurrency = baseCurrency,
                exchange = ::actInput then exchangeAct
            )
        )
    } then { values ->
        IncomeExpenseTransferPair(
            income = values[0],
            expense = values[1],
            transferIncome = values[2],
            transferExpense = values[3]
        )
    }

    data class Input(
        val transactions: List<com.spgrvl.kissos.base.legacy.Transaction>,
        val baseCurrency: String,
        val accounts: List<Account>
    )
}

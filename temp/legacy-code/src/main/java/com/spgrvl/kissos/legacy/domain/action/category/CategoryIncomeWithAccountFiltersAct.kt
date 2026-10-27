package com.spgrvl.kissos.wallet.domain.action.category

import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.temp.migration.getAccountId
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.action.transaction.CalcTrnsIncomeExpenseAct
import com.spgrvl.kissos.wallet.domain.action.transaction.LegacyCalcTrnsIncomeExpenseAct
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpenseTransferPair
import javax.inject.Inject

class CategoryIncomeWithAccountFiltersAct @Inject constructor(
    private val calcTrnsIncomeExpenseAct: CalcTrnsIncomeExpenseAct
) : FPAction<CategoryIncomeWithAccountFiltersAct.Input, IncomeExpenseTransferPair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        transactions.filter {
            it.category?.value == category?.id?.value
        }.filter {
            if (accountFilterSet.isEmpty()) {
                true
            } else {
                accountFilterSet.contains(it.getAccountId())
            }
        }
    } then {
        CalcTrnsIncomeExpenseAct.Input(
            transactions = it,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    } then calcTrnsIncomeExpenseAct

    data class Input(
        val transactions: List<Transaction>,
        val accountFilterList: List<Account>,
        val category: Category?,
        val baseCurrency: String
    )
}

@Deprecated("Uses legacy Transaction")
class LegacyCategoryIncomeWithAccountFiltersAct @Inject constructor(
    private val calcTrnsIncomeExpenseAct: LegacyCalcTrnsIncomeExpenseAct
) : FPAction<LegacyCategoryIncomeWithAccountFiltersAct.Input, IncomeExpenseTransferPair>() {

    override suspend fun Input.compose(): suspend () -> IncomeExpenseTransferPair = suspend {
        val accountFilterSet = accountFilterList.map { it.id }.toHashSet()
        transactions.filter {
            it.categoryId == category?.id?.value
        }.filter {
            if (accountFilterSet.isEmpty()) {
                true
            } else {
                accountFilterSet.contains(it.accountId)
            }
        }
    } then {
        LegacyCalcTrnsIncomeExpenseAct.Input(
            transactions = it,
            baseCurrency = baseCurrency,
            accounts = accountFilterList
        )
    } then calcTrnsIncomeExpenseAct

    data class Input(
        val transactions: List<com.spgrvl.kissos.base.legacy.Transaction>,
        val accountFilterList: List<Account>,
        val category: Category?,
        val baseCurrency: String
    )
}

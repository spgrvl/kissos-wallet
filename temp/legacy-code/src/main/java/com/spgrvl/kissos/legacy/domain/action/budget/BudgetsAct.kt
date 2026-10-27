package com.spgrvl.kissos.wallet.domain.action.budget

import com.spgrvl.kissos.data.db.dao.read.BudgetDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenMap
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Budget
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class BudgetsAct @Inject constructor(
    private val budgetDao: BudgetDao
) : FPAction<Unit, ImmutableList<Budget>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Budget> = suspend {
        budgetDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}

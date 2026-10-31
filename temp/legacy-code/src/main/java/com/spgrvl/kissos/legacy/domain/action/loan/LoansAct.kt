package com.spgrvl.kissos.wallet.domain.action.loan

import com.spgrvl.kissos.data.db.dao.read.LoanDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenMap
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Loan
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class LoansAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<Unit, ImmutableList<Loan>>() {
    override suspend fun Unit.compose(): suspend () -> ImmutableList<Loan> = suspend {
        loanDao.findAll()
    } thenMap { it.toLegacyDomain() } then { it.toImmutableList() }
}

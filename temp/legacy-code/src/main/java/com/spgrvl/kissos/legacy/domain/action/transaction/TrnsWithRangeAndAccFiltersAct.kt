package com.spgrvl.kissos.wallet.domain.action.transaction

import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.data.db.dao.read.TransactionDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenFilter
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class TrnsWithRangeAndAccFiltersAct @Inject constructor(
    private val transactionDao: TransactionDao
) : FPAction<TrnsWithRangeAndAccFiltersAct.Input, List<Transaction>>() {

    override suspend fun Input.compose(): suspend () -> List<Transaction> = suspend {
        transactionDao.findAllBetween(range.from(), range.to())
            .map { it.toLegacyDomain() }
    } thenFilter {
        accountIdFilterSet.contains(it.accountId) || accountIdFilterSet.contains(it.toAccountId)
    }

    data class Input(
        val range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange,
        val accountIdFilterSet: Set<UUID>
    )
}

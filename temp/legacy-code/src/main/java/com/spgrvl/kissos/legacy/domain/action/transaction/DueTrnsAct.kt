package com.spgrvl.kissos.wallet.domain.action.transaction

import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import javax.inject.Inject

class DueTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<ClosedTimeRange, List<Transaction>>() {

    override suspend fun ClosedTimeRange.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionRepository.findAllDueToBetween(
                startDate = from,
                endDate = to
            )
        }
    }
}

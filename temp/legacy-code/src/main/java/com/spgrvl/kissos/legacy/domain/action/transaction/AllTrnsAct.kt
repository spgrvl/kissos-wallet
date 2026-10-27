package com.spgrvl.kissos.wallet.domain.action.transaction

import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.frp.action.FPAction
import javax.inject.Inject

class AllTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<Unit, List<Transaction>>() {
    override suspend fun Unit.compose(): suspend () -> List<Transaction> = suspend {
        transactionRepository.findAll()
    }
}

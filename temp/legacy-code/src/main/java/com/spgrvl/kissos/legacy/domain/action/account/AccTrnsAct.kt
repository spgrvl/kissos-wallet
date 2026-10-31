package com.spgrvl.kissos.wallet.domain.action.account

import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import java.util.UUID
import javax.inject.Inject

class AccTrnsAct @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FPAction<AccTrnsAct.Input, List<Transaction>>() {
    override suspend fun Input.compose(): suspend () -> List<Transaction> = suspend {
        io {
            transactionRepository.findAllByAccountAndBetween(
                accountId = AccountId(accountId),
                startDate = range.from,
                endDate = range.to
            ) + transactionRepository.findAllToAccountAndBetween(
                toAccountId = AccountId(accountId),
                startDate = range.from,
                endDate = range.to
            )
        }
    }

    class Input(
        val accountId: UUID,
        val range: ClosedTimeRange
    )
}

package com.spgrvl.kissos.wallet.domain.action.viewmodel.home

import com.spgrvl.kissos.data.db.dao.read.TransactionDao
import com.spgrvl.kissos.frp.action.FPAction
import javax.inject.Inject

class HasTrnsAct @Inject constructor(
    private val transactionDao: TransactionDao
) : FPAction<Unit, Boolean>() {
    override suspend fun Unit.compose(): suspend () -> Boolean = suspend {
        io {
            transactionDao.findAll_LIMIT_1().isNotEmpty()
        }
    }
}

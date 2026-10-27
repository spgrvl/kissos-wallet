package com.spgrvl.kissos.wallet.domain.action.loan

import com.spgrvl.kissos.data.db.dao.read.LoanDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.legacy.datamodel.Loan
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class LoanByIdAct @Inject constructor(
    private val loanDao: LoanDao
) : FPAction<UUID, Loan?>() {
    override suspend fun UUID.compose(): suspend () -> Loan? = suspend {
        loanDao.findById(this)?.toLegacyDomain()
    }
}

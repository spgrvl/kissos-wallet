package com.spgrvl.kissos.wallet.domain.action.account

import com.spgrvl.kissos.data.db.dao.read.AccountDao
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

class AccountByIdAct @Inject constructor(
    private val accountDao: AccountDao
) : FPAction<UUID, Account?>() {
    @Deprecated("Legacy code. Don't use it, please.")
    override suspend fun UUID.compose(): suspend () -> Account? = suspend {
        this // accountId
    } then accountDao::findById then {
        it?.toLegacyDomain()
    }
}

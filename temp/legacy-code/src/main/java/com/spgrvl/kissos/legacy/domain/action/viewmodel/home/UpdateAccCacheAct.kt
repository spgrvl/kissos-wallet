package com.spgrvl.kissos.wallet.domain.action.viewmodel.home

import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.legacy.datamodel.Account
import javax.inject.Inject

class UpdateAccCacheAct @Inject constructor(
    private val kissosWalletCtx: KissosWalletCtx
) : FPAction<List<Account>, List<Account>>() {
    override suspend fun List<Account>.compose(): suspend () -> List<Account> = suspend {
        val accounts = this

        kissosWalletCtx.accountMap.clear()
        kissosWalletCtx.accountMap.putAll(accounts.map { it.id to it })

        accounts
    }
}

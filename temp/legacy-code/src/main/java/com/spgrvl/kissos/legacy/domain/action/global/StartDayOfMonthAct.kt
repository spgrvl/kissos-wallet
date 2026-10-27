package com.spgrvl.kissos.wallet.domain.action.global

import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.IvyWalletCtx
import com.spgrvl.kissos.base.legacy.SharedPrefs
import javax.inject.Inject

class StartDayOfMonthAct @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val ivyWalletCtx: IvyWalletCtx
) : FPAction<Unit, Int>() {

    override suspend fun Unit.compose(): suspend () -> Int = suspend {
        sharedPrefs.getInt(SharedPrefs.START_DATE_OF_MONTH, 1)
    } then { startDay ->
        ivyWalletCtx.setStartDayOfMonth(startDay)
        startDay
    }
}

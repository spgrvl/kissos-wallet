package com.spgrvl.kissos.wallet.domain.action.global

import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.monad.Res
import com.spgrvl.kissos.frp.monad.thenIfSuccess
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.base.legacy.SharedPrefs
import javax.inject.Inject

class UpdateStartDayOfMonthAct @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val kissosWalletCtx: KissosWalletCtx
) : FPAction<Int, Res<String, Int>>() {

    override suspend fun Int.compose(): suspend () -> Res<String, Int> = suspend {
        val startDay = this

        if (startDay in 1..31) {
            Res.Ok(startDay)
        } else {
            Res.Err("Invalid start day $startDay. Start date must be between 1 and 31.")
        }
    } thenIfSuccess { startDay ->
        sharedPrefs.putInt(SharedPrefs.START_DATE_OF_MONTH, startDay)
        kissosWalletCtx.setStartDayOfMonth(startDay)
        Res.Ok(startDay)
    } thenIfSuccess { startDay ->
        kissosWalletCtx.initSelectedPeriodInMemory(
            startDayOfMonth = startDay,
            forceReinitialize = true
        )
        Res.Ok(startDay)
    }
}

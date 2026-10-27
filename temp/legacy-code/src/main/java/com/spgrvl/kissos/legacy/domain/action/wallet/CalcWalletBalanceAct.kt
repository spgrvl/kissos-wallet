package com.spgrvl.kissos.wallet.domain.action.wallet

import arrow.core.toOption
import com.spgrvl.kissos.data.model.Account
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.model.primitive.ColorInt
import com.spgrvl.kissos.data.model.primitive.IconAsset
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.action.thenFilter
import com.spgrvl.kissos.frp.action.thenMap
import com.spgrvl.kissos.frp.action.thenSum
import com.spgrvl.kissos.frp.fixUnit
import com.spgrvl.kissos.wallet.domain.action.account.AccountsAct
import com.spgrvl.kissos.wallet.domain.action.account.CalcAccBalanceAct
import com.spgrvl.kissos.wallet.domain.action.exchange.ExchangeAct
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import com.spgrvl.kissos.wallet.domain.pure.exchange.ExchangeData
import java.math.BigDecimal
import javax.inject.Inject

class CalcWalletBalanceAct @Inject constructor(
    private val accountsAct: AccountsAct,
    private val calcAccBalanceAct: CalcAccBalanceAct,
    private val exchangeAct: ExchangeAct,
) : FPAction<CalcWalletBalanceAct.Input, BigDecimal>() {

    override suspend fun Input.compose(): suspend () -> BigDecimal = recipe().fixUnit()

    private suspend fun Input.recipe(): suspend (Unit) -> BigDecimal =
        accountsAct thenFilter {
            withExcluded || it.includeInBalance
        } thenMap { account ->
            calcAccBalanceAct(
                CalcAccBalanceAct.Input(
                    account = Account(
                        id = AccountId(account.id),
                        name = NotBlankTrimmedString.from(account.name).getOrNull()
                            ?: error("account name cannot be blank"),
                        asset = AssetCode.from(account.currency ?: baseCurrency).getOrNull()
                            ?: error("account currency cannot be blank"),
                        color = ColorInt(account.color),
                        icon = account.icon?.let { IconAsset.from(it).getOrNull() },
                        includeInBalance = account.includeInBalance,
                        orderNum = account.orderNum,
                    ),
                    range = range
                )
            )
        } thenMap {
            exchangeAct(
                ExchangeAct.Input(
                    data = ExchangeData(
                        baseCurrency = baseCurrency,
                        fromCurrency = (it.account.asset.code).toOption(),
                        toCurrency = balanceCurrency
                    ),
                    amount = it.balance
                )
            )
        } thenSum {
            it.orNull() ?: BigDecimal.ZERO
        }

    @Suppress("DataClassDefaultValues")
    data class Input(
        val baseCurrency: String,
        val balanceCurrency: String = baseCurrency,
        val range: ClosedTimeRange? = null,
        val withExcluded: Boolean = false
    )
}

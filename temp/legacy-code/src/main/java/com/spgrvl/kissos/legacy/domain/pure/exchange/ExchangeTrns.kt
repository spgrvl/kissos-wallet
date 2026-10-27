package com.spgrvl.kissos.wallet.domain.pure.exchange

import arrow.core.Option
import arrow.core.toOption
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.temp.migration.getAccountId
import com.spgrvl.kissos.data.temp.migration.getValue
import com.spgrvl.kissos.frp.Pure
import com.spgrvl.kissos.frp.SideEffect
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.pure.account.accountCurrency
import com.spgrvl.kissos.wallet.domain.pure.transaction.LegacyTrnFunctions
import com.spgrvl.kissos.wallet.domain.pure.transaction.trnCurrency
import java.math.BigDecimal
import java.util.UUID

typealias ExchangeEffect = suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>

data class ExchangeTrnArgument(
    val baseCurrency: String,
    @SideEffect
    val getAccount: suspend (accountId: UUID) -> Account?,
    @SideEffect
    val exchange: ExchangeEffect
)

@Pure
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    arg: ExchangeTrnArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.getAccountId())?.let {
        accountCurrency(it, arg.baseCurrency)
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        trnCurrency = fromCurrency,
        toCurrency = arg.baseCurrency,
        exchange = arg.exchange
    )
}

@Deprecated("Uses legacy Transaction")
@Pure
suspend fun exchangeInBaseCurrency(
    transaction: com.spgrvl.kissos.base.legacy.Transaction,
    arg: ExchangeTrnArgument
): BigDecimal {
    val fromCurrency = arg.getAccount(transaction.accountId)?.let {
        accountCurrency(it, arg.baseCurrency)
    }.toOption()

    return exchangeInCurrency(
        transaction = transaction,
        baseCurrency = arg.baseCurrency,
        trnCurrency = fromCurrency,
        toCurrency = arg.baseCurrency,
        exchange = arg.exchange
    )
}

@Pure
suspend fun exchangeInBaseCurrency(
    transaction: Transaction,
    baseCurrency: String,
    accounts: List<Account>,

    @SideEffect
    exchange: ExchangeEffect
): BigDecimal = exchangeInCurrency(
    transaction = transaction,
    baseCurrency = baseCurrency,
    accounts = accounts,
    toCurrency = baseCurrency,
    exchange = exchange
)

@Pure
suspend fun exchangeInCurrency(
    transaction: Transaction,
    baseCurrency: String,
    accounts: List<Account>,
    toCurrency: String,

    @SideEffect
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = trnCurrency(transaction, accounts, baseCurrency),
            toCurrency = toCurrency
        ),
        transaction.getValue()
    ).orNull() ?: BigDecimal.ZERO
}

suspend fun exchangeInCurrency(
    transaction: Transaction,
    baseCurrency: String,
    trnCurrency: Option<String>,
    toCurrency: String,

    @SideEffect
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = trnCurrency,
            toCurrency = toCurrency
        ),
        transaction.getValue()
    ).orNull() ?: BigDecimal.ZERO
}

@Deprecated("Uses legacy Transaction")
suspend fun exchangeInCurrency(
    transaction: com.spgrvl.kissos.base.legacy.Transaction,
    baseCurrency: String,
    trnCurrency: Option<String>,
    toCurrency: String,

    @SideEffect
    exchange: ExchangeEffect
): BigDecimal {
    return exchange(
        ExchangeData(
            baseCurrency = baseCurrency,
            fromCurrency = trnCurrency,
            toCurrency = toCurrency
        ),
        transaction.amount
    ).orNull() ?: BigDecimal.ZERO
}

@Deprecated("Uses legacy Transaction")
object LegacyExchangeTrns {

    @Pure
    suspend fun exchangeInBaseCurrency(
        transaction: com.spgrvl.kissos.base.legacy.Transaction,
        baseCurrency: String,
        accounts: List<Account>,

        @SideEffect
        exchange: ExchangeEffect
    ): BigDecimal = exchangeInCurrency(
        transaction = transaction,
        baseCurrency = baseCurrency,
        accounts = accounts,
        toCurrency = baseCurrency,
        exchange = exchange
    )

    @Pure
    suspend fun exchangeInCurrency(
        transaction: com.spgrvl.kissos.base.legacy.Transaction,
        baseCurrency: String,
        accounts: List<Account>,
        toCurrency: String,

        @SideEffect
        exchange: ExchangeEffect
    ): BigDecimal {
        return exchange(
            ExchangeData(
                baseCurrency = baseCurrency,
                fromCurrency = LegacyTrnFunctions.trnCurrency(transaction, accounts, baseCurrency),
                toCurrency = toCurrency
            ),
            transaction.amount
        ).orNull() ?: BigDecimal.ZERO
    }
}
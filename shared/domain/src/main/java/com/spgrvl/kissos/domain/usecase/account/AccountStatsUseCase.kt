package com.spgrvl.kissos.domain.usecase.account

import arrow.core.Option
import com.spgrvl.kissos.base.threading.DispatchersProvider
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.Expense
import com.spgrvl.kissos.data.model.Income
import com.spgrvl.kissos.data.model.PositiveValue
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.model.Transfer
import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.domain.model.StatSummary
import com.spgrvl.kissos.domain.model.TimeRange
import com.spgrvl.kissos.domain.usecase.StatSummaryBuilder
import com.spgrvl.kissos.domain.usecase.exchange.ExchangeUseCase
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("UnusedPrivateProperty", "UnusedParameter")
class AccountStatsUseCase @Inject constructor(
    private val dispatchers: DispatchersProvider,
    private val accountRepository: AccountRepository,
    private val exchangeUseCase: ExchangeUseCase,
) {

    suspend fun calculate(
        account: AccountId,
        range: TimeRange,
        outCurrency: AssetCode
    ): ExchangedAccountStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        account: AccountId,
        range: TimeRange,
        transactions: List<Transaction>,
    ): ExchangedAccountStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        account: AccountId,
        range: TimeRange
    ): AccountStats {
        TODO("Not implemented")
    }

    suspend fun calculate(
        account: AccountId,
        transactions: List<Transaction>
    ): AccountStats = withContext(dispatchers.default) {
        val income = StatSummaryBuilder()
        val expense = StatSummaryBuilder()
        val transfersIn = StatSummaryBuilder()
        val transfersOut = StatSummaryBuilder()

        for (trn in transactions) {
            when (trn) {
                is Expense -> if (trn.account == account) {
                    expense.process(trn.value)
                }

                is Income -> if (trn.account == account) {
                    income.process(trn.value)
                }

                is Transfer -> {
                    when (account) {
                        trn.fromAccount -> transfersOut.process(trn.fromValue)
                        trn.toAccount -> transfersIn.process(trn.toValue)
                        else -> {
                            // ignore, not relevant transfer for the account
                        }
                    }
                }
            }
        }

        AccountStats(
            income = income.build(),
            expense = expense.build(),
            transfersIn = transfersIn.build(),
            transfersOut = transfersOut.build()
        )
    }
}

data class AccountStats(
    val income: StatSummary,
    val expense: StatSummary,
    val transfersIn: StatSummary,
    val transfersOut: StatSummary,
) {
    companion object {
        val Zero = AccountStats(
            income = StatSummary.Zero,
            expense = StatSummary.Zero,
            transfersIn = StatSummary.Zero,
            transfersOut = StatSummary.Zero,
        )
    }
}

data class ExchangedAccountStats(
    val income: Option<PositiveValue>,
    val expense: Option<PositiveValue>,
    val transfersIn: Option<PositiveValue>,
    val transfersOut: Option<PositiveValue>,
    val exchangeErrors: Set<AssetCode>,
)
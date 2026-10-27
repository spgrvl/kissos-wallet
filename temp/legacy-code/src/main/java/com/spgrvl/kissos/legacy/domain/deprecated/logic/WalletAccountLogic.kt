package com.spgrvl.kissos.wallet.domain.deprecated.logic

import arrow.core.getOrElse
import com.spgrvl.kissos.base.legacy.SharedPrefs
import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.Expense
import com.spgrvl.kissos.data.model.Income
import com.spgrvl.kissos.data.repository.CurrencyRepository
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.data.repository.mapper.TransactionMapper
import com.spgrvl.kissos.data.temp.migration.getValue
import com.spgrvl.kissos.legacy.data.model.filterOverdue
import com.spgrvl.kissos.legacy.data.model.filterUpcoming
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.legacy.datamodel.temp.toDomain
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.domain.action.viewmodel.account.AccountDataAct
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

@Deprecated("Migrate to FP Style")
class WalletAccountLogic @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val accountDataAct: AccountDataAct,
    private val sharedPrefs: SharedPrefs,
    private val currencyRepository: CurrencyRepository,
    private val timeProvider: TimeProvider
) {

    suspend fun adjustBalance(
        account: Account,
        actualBalance: Double? = null,
        newBalance: Double,

        adjustTransactionTitle: String = stringRes(R.string.adjust_balance),

        isFiat: Boolean? = null,
        trnIsSyncedFlag: Boolean = false, // TODO: Remove this once Bank Integration trn sync is properly implemented
    ) {
        val ab = actualBalance ?: calculateAccountBalance(account)
        val diff = ab - newBalance

        val finalDiff = if (isFiat == true && abs(diff) < 0.009) 0.0 else diff
        when {
            finalDiff < 0 -> {
                // add income
                Transaction(
                    type = TransactionType.INCOME,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                    isSynced = trnIsSyncedFlag
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }

            finalDiff > 0 -> {
                // add expense
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = adjustTransactionTitle,
                    amount = diff.absoluteValue.toBigDecimal(),
                    toAmount = diff.absoluteValue.toBigDecimal(),
                    dateTime = timeProvider.utcNow(),
                    accountId = account.id,
                    isSynced = trnIsSyncedFlag
                ).toDomain(transactionMapper)?.let {
                    transactionRepository.save(it)
                }
            }
        }
    }

    suspend fun calculateAccountBalance(
        account: Account
    ): Double {
        val accountList = account.toDomainAccount(currencyRepository)
            .map { a -> listOf(a) }
            .getOrElse { emptyList() }

        val includeTransfersInCalc =
            sharedPrefs.getBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, false)

        val accountsDataList = accountDataAct(
            AccountDataAct.Input(
                accounts = accountList.toImmutableList(),
                range = ClosedTimeRange.allTimeIvy(timeProvider),
                baseCurrency = currencyRepository.getBaseCurrency().code,
                includeTransfersInCalc = includeTransfersInCalc
            )
        )

        return accountsDataList.firstOrNull()?.balance ?: 0.0
    }

    suspend fun calculateUpcomingIncome(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): Double =
        upcoming(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateUpcomingExpenses(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): Double =
        upcoming(account = account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueIncome(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Income>()
            .sumOf { it.getValue().toDouble() }

    suspend fun calculateOverdueExpenses(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): Double =
        overdue(account, range = range)
            .filterIsInstance<Expense>()
            .sumOf { it.getValue().toDouble() }

    suspend fun upcoming(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming()
    }

    suspend fun overdue(
        account: Account,
        range: com.spgrvl.kissos.legacy.data.model.FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByAccount(
            accountId = AccountId(account.id),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue()
    }
}

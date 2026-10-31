package com.spgrvl.kissos.wallet.domain.deprecated.logic

import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.read.AccountDao
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.data.repository.mapper.TransactionMapper
import com.spgrvl.kissos.legacy.data.model.FromToTimeRange
import com.spgrvl.kissos.legacy.data.model.filterOverdue
import com.spgrvl.kissos.legacy.data.model.filterOverdueLegacy
import com.spgrvl.kissos.legacy.data.model.filterUpcoming
import com.spgrvl.kissos.legacy.data.model.filterUpcomingLegacy
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacy
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import com.spgrvl.kissos.legacy.domain.pure.transaction.LegacyTrnDateDividers
import com.spgrvl.kissos.wallet.domain.deprecated.logic.currency.ExchangeRatesLogic
import com.spgrvl.kissos.wallet.domain.deprecated.logic.currency.sumInBaseCurrency
import java.util.UUID
import javax.inject.Inject

@Deprecated("Migrate to FP Style")
class WalletCategoryLogic @Inject constructor(
    private val accountDao: AccountDao,
    private val settingsDao: SettingsDao,
    private val exchangeRatesLogic: ExchangeRatesLogic,
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) {

    suspend fun calculateCategoryBalance(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        transactions: List<Transaction> = emptyList()
    ): Double {
        val baseCurrency = settingsDao.findFirst().currency
        val accounts = accountDao.findAll().map { it.toLegacyDomain() }

        return historyByCategory(
            category,
            range = range,
            accountFilterSet = accountFilterSet,
            transactions = transactions
        )
            .sumOf {
                val amount = exchangeRatesLogic.amountBaseCurrency(
                    transaction = it,
                    baseCurrency = baseCurrency,
                    accounts = accounts
                )

                when (it.type) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0 // TODO: Transfer zero operation
                }
            }
    }

    suspend fun calculateCategoryIncome(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
    ): Double {
        return transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryIncome(
        incomeTransaction: List<Transaction>,
        accountFilterSet: Set<UUID> = emptySet()
    ): Double {
        return incomeTransaction
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryExpenses(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
    ): Double {
        return transactionRepository
            .findAllByCategoryAndTypeAndBetween(
                categoryId = category.id.value,
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateCategoryExpenses(
        expenseTransactions: List<Transaction>,
        accountFilterSet: Set<UUID> = emptySet()
    ): Double {
        return expenseTransactions
            .filter {
                accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
            }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateUnspecifiedBalance(range: FromToTimeRange): Double {
        return calculateUnspecifiedIncome(range) - calculateUnspecifiedExpenses(range)
    }

    suspend fun calculateUnspecifiedIncome(range: FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.INCOME,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateUnspecifiedExpenses(range: FromToTimeRange): Double {
        return transactionRepository
            .findAllUnspecifiedAndTypeAndBetween(
                type = TransactionType.EXPENSE,
                startDate = range.from(),
                endDate = range.to()
            ).map { it.toLegacy(transactionMapper) }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun historyByCategoryAccountWithDateDividers(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID>,
        transactions: List<Transaction> = emptyList()
    ): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            historyByCategory(category, range, transactions = transactions)
                .filter {
                    accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
                }
                .withDateDividers(
                    exchangeRatesLogic = exchangeRatesLogic,
                    settingsDao = settingsDao,
                    accountDao = accountDao,
                    timeConverter = timeConverter,
                )
        }
    }

    suspend fun historyByCategory(
        category: Category,
        range: FromToTimeRange,
        accountFilterSet: Set<UUID> = emptySet(),
        transactions: List<Transaction> = emptyList()
    ): List<Transaction> {
        val trans = transactions.ifEmpty {
            transactionRepository
                .findAllByCategoryAndBetween(
                    categoryId = category.id.value,
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacy(transactionMapper) }
        }

        return trans.filter {
            accountFilterSet.isEmpty() || accountFilterSet.contains(it.accountId)
        }
    }

    suspend fun historyUnspecified(range: FromToTimeRange): List<TransactionHistoryItem> {
        return with(LegacyTrnDateDividers) {
            transactionRepository
                .findAllUnspecifiedAndBetween(
                    startDate = range.from(),
                    endDate = range.to()
                ).map { it.toLegacy(transactionMapper) }
                .withDateDividers(
                    exchangeRatesLogic = exchangeRatesLogic,
                    settingsDao = settingsDao,
                    accountDao = accountDao,
                    timeConverter = timeConverter,
                )
        }
    }

    suspend fun calculateUpcomingIncomeByCategory(
        category: Category,
        range: FromToTimeRange
    ): Double {
        return upcomingByCategoryLegacy(category = category, range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingExpensesByCategory(
        category: Category,
        range: FromToTimeRange
    ): Double {
        return upcomingByCategoryLegacy(category = category, range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingIncomeUnspecified(range: FromToTimeRange): Double {
        return upcomingUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateUpcomingExpensesUnspecified(range: FromToTimeRange): Double {
        return upcomingUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    @Deprecated("Uses legacy Transaction")
    suspend fun upcomingByCategoryLegacy(
        category: Category,
        range: FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterUpcomingLegacy(timeProvider, timeConverter)
    }

    suspend fun upcomingByCategory(
        category: Category,
        range: FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming()
    }

    @Deprecated("Uses legacy Transaction")
    suspend fun upcomingUnspecifiedLegacy(range: FromToTimeRange): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterUpcomingLegacy(timeProvider, timeConverter)
    }

    suspend fun upcomingUnspecified(
        range: FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.upcomingFrom(timeProvider),
            endDate = range.to()
        ).filterUpcoming()
    }

    suspend fun calculateOverdueIncomeByCategory(
        category: Category,
        range: FromToTimeRange
    ): Double {
        return overdueByCategoryLegacy(category, range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueExpensesByCategory(
        category: Category,
        range: FromToTimeRange
    ): Double {
        return overdueByCategoryLegacy(category, range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueIncomeUnspecified(range: FromToTimeRange): Double {
        return overdueUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.INCOME }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    suspend fun calculateOverdueExpensesUnspecified(range: FromToTimeRange): Double {
        return overdueUnspecifiedLegacy(range = range)
            .filter { it.type == TransactionType.EXPENSE }
            .sumInBaseCurrency(
                exchangeRatesLogic = exchangeRatesLogic,
                settingsDao = settingsDao,
                accountDao = accountDao
            )
    }

    @Deprecated("Uses legacy Transaction")
    suspend fun overdueByCategoryLegacy(
        category: Category,
        range: FromToTimeRange
    ): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterOverdueLegacy(timeProvider, timeConverter)
    }

    suspend fun overdueByCategory(
        category: Category,
        range: FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategory(
            categoryId = CategoryId(category.id.value),
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .filterOverdue()
    }

    @Deprecated("Uses legacy Transaction")
    suspend fun overdueUnspecifiedLegacy(range: FromToTimeRange): List<Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        )
            .map {
                it.toLegacy(transactionMapper)
            }
            .filterOverdueLegacy(timeProvider, timeConverter)
    }

    suspend fun overdueUnspecified(
        range: FromToTimeRange
    ): List<com.spgrvl.kissos.data.model.Transaction> {
        return transactionRepository.findAllDueToBetweenByCategoryUnspecified(
            startDate = range.from(),
            endDate = range.overdueTo(timeProvider)
        ).filterOverdue()
    }
}

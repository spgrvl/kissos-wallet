package com.spgrvl.kissos.legacy.domain.pure.transaction

import arrow.core.Option
import arrow.core.toOption
import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.base.time.convertToLocal
import com.spgrvl.kissos.data.db.dao.read.AccountDao
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.data.model.Tag
import com.spgrvl.kissos.data.model.TagId
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.TagRepository
import com.spgrvl.kissos.data.repository.mapper.TransactionMapper
import com.spgrvl.kissos.frp.Pure
import com.spgrvl.kissos.frp.SideEffect
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.legacy.datamodel.temp.toImmutableLegacyTags
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import com.spgrvl.kissos.legacy.utils.toEpochSeconds
import com.spgrvl.kissos.wallet.domain.data.TransactionHistoryDateDivider
import com.spgrvl.kissos.wallet.domain.deprecated.logic.currency.ExchangeRatesLogic
import com.spgrvl.kissos.wallet.domain.pure.exchange.ExchangeData
import com.spgrvl.kissos.wallet.domain.pure.exchange.ExchangeTrnArgument
import com.spgrvl.kissos.wallet.domain.pure.exchange.exchangeInBaseCurrency
import com.spgrvl.kissos.wallet.domain.pure.transaction.LegacyFoldTransactions
import com.spgrvl.kissos.wallet.domain.pure.transaction.LegacyTrnFunctions
import com.spgrvl.kissos.wallet.domain.pure.transaction.expenses
import com.spgrvl.kissos.wallet.domain.pure.transaction.incomes
import com.spgrvl.kissos.wallet.domain.pure.transaction.sumTrns
import java.math.BigDecimal
import java.util.UUID

@Deprecated("Migrate to actions")
suspend fun List<Transaction>.withDateDividers(
    exchangeRatesLogic: ExchangeRatesLogic,
    settingsDao: SettingsDao,
    accountDao: AccountDao,
    tagRepository: TagRepository,
    accountRepository: AccountRepository,
): List<TransactionHistoryItem> {
    return transactionsWithDateDividers(
        transactions = this,
        baseCurrencyCode = settingsDao.findFirst().currency,
        getAccount = accountDao::findById then { it?.toLegacyDomain() },
        getTags = { tagsIds -> tagRepository.findByIds(tagsIds) },
        accountRepository = accountRepository,
        exchange = { data, amount ->
            exchangeRatesLogic.convertAmount(
                baseCurrency = data.baseCurrency,
                fromCurrency = data.fromCurrency.orNull() ?: "",
                toCurrency = data.toCurrency,
                amount = amount.toDouble()
            ).toBigDecimal().toOption()
        }
    )
}

@Pure
suspend fun transactionsWithDateDividers(
    transactions: List<Transaction>,
    baseCurrencyCode: String,
    accountRepository: AccountRepository,
    @SideEffect
    getAccount: suspend (accountId: UUID) -> Account?,
    @SideEffect
    exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>,
    @SideEffect
    getTags: suspend (tagIds: List<TagId>) -> List<Tag> = { emptyList() },
): List<TransactionHistoryItem> {
    if (transactions.isEmpty()) return emptyList()
    val transactionsMapper = TransactionMapper(accountRepository)
    return transactions
        .groupBy { it.time.convertToLocal().toLocalDate() }
        .filterKeys { it != null }
        .toSortedMap { date1, date2 ->
            if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
            (date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay().toEpochSeconds()).toInt()
        }
        .flatMap { (date, transactionsForDate) ->
            val arg = ExchangeTrnArgument(
                baseCurrency = baseCurrencyCode,
                getAccount = getAccount,
                exchange = exchange
            )

            // Required to be interoperable with [TransactionHistoryItem]
            val legacyTransactionsForDate = with(transactionsMapper) {
                transactionsForDate.map {
                    it.toEntity()
                        .toLegacyDomain(tags = getTags(it.tags).toImmutableLegacyTags())
                }
            }
            listOf<TransactionHistoryItem>(
                TransactionHistoryDateDivider(
                    date = date!!,
                    income = sumTrns(
                        incomes(transactionsForDate),
                        ::exchangeInBaseCurrency,
                        arg
                    ).toDouble(),
                    expenses = sumTrns(
                        expenses(transactionsForDate),
                        ::exchangeInBaseCurrency,
                        arg
                    ).toDouble()
                ),
            ).plus(legacyTransactionsForDate)
        }
}

@Deprecated("Uses legacy Transaction")
object LegacyTrnDateDividers {
    @Deprecated("Migrate to actions")
    suspend fun List<com.spgrvl.kissos.base.legacy.Transaction>.withDateDividers(
        exchangeRatesLogic: ExchangeRatesLogic,
        settingsDao: SettingsDao,
        accountDao: AccountDao,
        timeConverter: TimeConverter,
    ): List<TransactionHistoryItem> {
        return transactionsWithDateDividers(
            transactions = this,
            baseCurrencyCode = settingsDao.findFirst().currency,
            getAccount = accountDao::findById then { it?.toLegacyDomain() },
            exchange = { data, amount ->
                exchangeRatesLogic.convertAmount(
                    baseCurrency = data.baseCurrency,
                    fromCurrency = data.fromCurrency.orNull() ?: "",
                    toCurrency = data.toCurrency,
                    amount = amount.toDouble()
                ).toBigDecimal().toOption()
            },
            timeConverter = timeConverter,
        )
    }

    @Pure
    suspend fun transactionsWithDateDividers(
        transactions: List<com.spgrvl.kissos.base.legacy.Transaction>,
        baseCurrencyCode: String,
        timeConverter: TimeConverter,

        @SideEffect
        getAccount: suspend (accountId: UUID) -> Account?,
        @SideEffect
        exchange: suspend (ExchangeData, BigDecimal) -> Option<BigDecimal>
    ): List<TransactionHistoryItem> {
        if (transactions.isEmpty()) return emptyList()

        return transactions
            .groupBy { with(timeConverter) { it.dateTime?.toLocalDate() } }
            .filterKeys { it != null }
            .toSortedMap { date1, date2 ->
                if (date1 == null || date2 == null) return@toSortedMap 0 // this case shouldn't happen
                (
                        date2.atStartOfDay().toEpochSeconds() - date1.atStartOfDay()
                            .toEpochSeconds()
                        ).toInt()
            }
            .flatMap { (date, transactionsForDate) ->
                val arg = ExchangeTrnArgument(
                    baseCurrency = baseCurrencyCode,
                    getAccount = getAccount,
                    exchange = exchange
                )

                listOf<TransactionHistoryItem>(
                    TransactionHistoryDateDivider(
                        date = date!!,
                        income = LegacyFoldTransactions.sumTrns(
                            LegacyTrnFunctions.incomes(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble(),
                        expenses = LegacyFoldTransactions.sumTrns(
                            LegacyTrnFunctions.expenses(transactionsForDate),
                            ::exchangeInBaseCurrency,
                            arg
                        ).toDouble()
                    ),
                ).plus(transactionsForDate)
            }
    }
}
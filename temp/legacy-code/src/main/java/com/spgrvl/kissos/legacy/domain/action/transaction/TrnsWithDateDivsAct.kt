package com.spgrvl.kissos.wallet.domain.action.transaction

import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.data.db.dao.read.AccountDao
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.TagRepository
import com.spgrvl.kissos.frp.action.FPAction
import com.spgrvl.kissos.frp.then
import com.spgrvl.kissos.legacy.datamodel.temp.toLegacyDomain
import com.spgrvl.kissos.legacy.domain.pure.transaction.LegacyTrnDateDividers
import com.spgrvl.kissos.legacy.domain.pure.transaction.transactionsWithDateDividers
import com.spgrvl.kissos.wallet.domain.action.exchange.ExchangeAct
import com.spgrvl.kissos.wallet.domain.action.exchange.actInput
import javax.inject.Inject

class TrnsWithDateDivsAct @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAct: ExchangeAct,
    private val tagRepository: TagRepository,
    private val accountRepository: AccountRepository,
) : FPAction<TrnsWithDateDivsAct.Input, List<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> List<TransactionHistoryItem> = suspend {
        transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,
            getTags = { tagIds -> tagRepository.findByIds(tagIds) },
            getAccount = accountDao::findById then { it?.toLegacyDomain() },
            accountRepository = accountRepository,
            exchange = ::actInput then exchangeAct
        )
    }

    data class Input(
        val baseCurrency: String,
        val transactions: List<com.spgrvl.kissos.data.model.Transaction>
    )
}

@Deprecated("Uses legacy Transaction")
class LegacyTrnsWithDateDivsAct @Inject constructor(
    private val accountDao: AccountDao,
    private val exchangeAct: ExchangeAct,
    private val timeConverter: TimeConverter,
) : FPAction<LegacyTrnsWithDateDivsAct.Input, List<TransactionHistoryItem>>() {

    override suspend fun Input.compose(): suspend () -> List<TransactionHistoryItem> = suspend {
        LegacyTrnDateDividers.transactionsWithDateDividers(
            transactions = transactions,
            baseCurrencyCode = baseCurrency,

            getAccount = accountDao::findById then { it?.toLegacyDomain() },
            exchange = ::actInput then exchangeAct,
            timeConverter = timeConverter,
        )
    }

    data class Input(
        val baseCurrency: String,
        val transactions: List<Transaction>
    )
}
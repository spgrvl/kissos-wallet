package com.spgrvl.kissos.wallet.domain.pure.transaction

import arrow.core.Option
import arrow.core.toOption
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.convertToLocal
import com.spgrvl.kissos.data.model.Expense
import com.spgrvl.kissos.data.model.Income
import com.spgrvl.kissos.data.model.Transaction
import com.spgrvl.kissos.data.model.Transfer
import com.spgrvl.kissos.data.temp.migration.getAccountId

import com.spgrvl.kissos.frp.Pure
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.pure.account.accountCurrency
import java.time.LocalDate

@Pure
fun expenses(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Expense>()
}

@Pure
fun incomes(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Income>()
}

@Pure
fun transfers(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Transfer>()
}

@Pure
fun isUpcoming(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isBefore(dueDate) || dateNow.isEqual(dueDate)
}

@Pure
fun isOverdue(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isAfter(dueDate)
}

@Pure
fun trnCurrency(
    transaction: Transaction,
    accounts: List<Account>,
    baseCurrency: String
): Option<String> {
    val account = accounts.find {
        it.id == transaction.getAccountId()
    }
        ?: return baseCurrency.toOption()
    return accountCurrency(account, baseCurrency).toOption()
}

@Deprecated("Uses legacy Transaction")
object LegacyTrnFunctions {
    @Pure
    fun expenses(
        transactions: List<com.spgrvl.kissos.base.legacy.Transaction>
    ): List<com.spgrvl.kissos.base.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
    }

    @Pure
    fun incomes(
        transactions: List<com.spgrvl.kissos.base.legacy.Transaction>
    ): List<com.spgrvl.kissos.base.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.INCOME }
    }

    @Pure
    fun trnCurrency(
        transaction: com.spgrvl.kissos.base.legacy.Transaction,
        accounts: List<Account>,
        baseCurrency: String
    ): Option<String> {
        val account = accounts.find { it.id == transaction.accountId }
            ?: return baseCurrency.toOption()
        return accountCurrency(account, baseCurrency).toOption()
    }
}

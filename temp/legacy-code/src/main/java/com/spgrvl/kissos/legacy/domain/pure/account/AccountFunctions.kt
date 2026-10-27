package com.spgrvl.kissos.wallet.domain.pure.account

import com.spgrvl.kissos.legacy.datamodel.Account

fun filterExcluded(accounts: List<Account>): List<Account> =
    accounts.filter { it.includeInBalance }

fun accountCurrency(account: Account, baseCurrency: String): String =
    account.currency ?: baseCurrency

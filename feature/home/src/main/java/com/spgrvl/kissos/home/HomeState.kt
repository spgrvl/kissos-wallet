package com.spgrvl.kissos.home

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.home.customerjourney.CustomerJourneyCardModel
import com.spgrvl.kissos.legacy.data.AppBaseData
import com.spgrvl.kissos.legacy.data.BufferInfo
import com.spgrvl.kissos.legacy.data.LegacyDueSection
import com.spgrvl.kissos.legacy.data.model.TimePeriod
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@Immutable
data class HomeState(
    val theme: Theme,
    val name: String,

    val period: TimePeriod,
    val baseData: AppBaseData,

    val history: ImmutableList<TransactionHistoryItem>,
    val stats: IncomeExpensePair,

    val balance: BigDecimal,

    val buffer: BufferInfo,

    val upcoming: LegacyDueSection,
    val overdue: LegacyDueSection,

    val customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    val hideBalance: Boolean,
    val hideIncome: Boolean,
    val expanded: Boolean,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

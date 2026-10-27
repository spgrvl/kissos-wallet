package com.spgrvl.kissos.piechart

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.legacy.data.model.TimePeriod
import com.spgrvl.kissos.wallet.ui.theme.modal.ChoosePeriodModalData
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Immutable
data class PieChartStatisticState(
    val transactionType: TransactionType,
    val period: TimePeriod,
    val baseCurrency: String,
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>,
    val selectedCategory: SelectedCategory?,
    val accountIdFilterList: ImmutableList<UUID>,
    val showCloseButtonOnly: Boolean,
    val filterExcluded: Boolean,
    val transactions: ImmutableList<Transaction>,
    val choosePeriodModal: ChoosePeriodModalData?
)

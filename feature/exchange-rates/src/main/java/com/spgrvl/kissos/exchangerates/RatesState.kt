package com.spgrvl.kissos.exchangerates

import com.spgrvl.kissos.exchangerates.data.RateUi
import kotlinx.collections.immutable.ImmutableList

data class RatesState(
    val baseCurrency: String,
    val manual: ImmutableList<RateUi>,
    val automatic: ImmutableList<RateUi>
)

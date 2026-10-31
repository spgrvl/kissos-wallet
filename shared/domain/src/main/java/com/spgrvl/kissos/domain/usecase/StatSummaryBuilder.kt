package com.spgrvl.kissos.domain.usecase

import com.spgrvl.kissos.data.model.PositiveValue
import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.model.primitive.NonNegativeInt
import com.spgrvl.kissos.data.model.primitive.PositiveDouble
import com.spgrvl.kissos.domain.model.StatSummary

class StatSummaryBuilder {
    private var count = 0
    private val values = mutableMapOf<AssetCode, PositiveDouble>()

    fun process(value: PositiveValue) {
        count++
        val asset = value.asset
        // Because it might overflow
        PositiveDouble.from(
            (values[asset]?.value ?: 0.0) + value.amount.value
        ).onRight { newValue ->
            values[asset] = newValue
        }
    }

    fun build(): StatSummary = StatSummary(
        trnCount = NonNegativeInt.unsafe(count),
        values = values,
    )
}
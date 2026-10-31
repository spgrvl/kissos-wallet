package com.spgrvl.kissos.domain.model

import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.model.primitive.NonNegativeInt
import com.spgrvl.kissos.data.model.primitive.PositiveDouble

data class StatSummary(
    val trnCount: NonNegativeInt,
    val values: Map<AssetCode, PositiveDouble>,
) {
    companion object {
        val Zero = StatSummary(
            values = emptyMap(),
            trnCount = NonNegativeInt.Zero
        )
    }
}
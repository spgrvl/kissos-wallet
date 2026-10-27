package com.spgrvl.kissos.piechart

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.data.model.Category

@Immutable
data class SelectedCategory(
    val category: Category // null - Unspecified
)

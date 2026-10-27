package com.spgrvl.kissos.design.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l1_buildingBlocks.data.KissosPadding

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun Modifier.kissosPadding(kissosPadding: KissosPadding): Modifier {
    return this.padding(
        top = kissosPadding.top ?: 0.dp,
        bottom = kissosPadding.bottom ?: 0.dp,
        start = kissosPadding.start ?: 0.dp,
        end = kissosPadding.end ?: 0.dp
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun padding(
    top: Dp? = null,
    start: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null
): KissosPadding {
    return KissosPadding(
        top = top,
        bottom = bottom,
        start = start,
        end = end
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun padding(
    horizontal: Dp? = null,
    vertical: Dp? = null
): KissosPadding {
    return KissosPadding(
        top = vertical,
        bottom = vertical,
        start = horizontal,
        end = horizontal
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun padding(
    all: Dp? = null
): KissosPadding {
    return KissosPadding(
        top = all,
        bottom = all,
        start = all,
        end = all
    )
}

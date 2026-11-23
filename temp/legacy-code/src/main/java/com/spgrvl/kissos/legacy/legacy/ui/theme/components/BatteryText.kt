package com.spgrvl.kissos.legacy.legacy.ui.theme.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect

/*
 * A battery's text is drawn twice, in contrasting colours: once clipped to the filled part (the
 * first `fill` of the width) and once clipped to the rest. It stays readable wherever the fill
 * ends, even in the middle of a word.
 */

/** The text colours of a battery: its main text and the smaller caption under it. */
@Immutable
internal data class BatteryTextColors(val text: Color, val caption: Color)

/** Draws only the part over a battery's fill. */
internal fun Modifier.overBatteryFill(fill: Double): Modifier = drawWithContent {
    clipRect(right = size.width * fill.coerceIn(0.0, 1.0).toFloat()) { this@drawWithContent.drawContent() }
}

/** Draws only the part past a battery's fill. */
internal fun Modifier.pastBatteryFill(fill: Double): Modifier = drawWithContent {
    clipRect(left = size.width * fill.coerceIn(0.0, 1.0).toFloat()) { this@drawWithContent.drawContent() }
}

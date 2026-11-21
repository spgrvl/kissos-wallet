package com.spgrvl.kissos.design.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.spgrvl.kissos.design.system.colors.KissosColors
import kotlin.math.ln

@Composable
fun KissosMaterial3Theme(
    isTrueBlack: Boolean,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) kissosDarkColorScheme(isTrueBlack) else kissosLightColorScheme(),
        content = content,
    )
}

private fun kissosLightColorScheme(): ColorScheme {
    val surface = KissosColors.White
    val surfaceTint = KissosColors.Black
    val surfaceVariant = KissosColors.ExtraLightGray
    return lightColorScheme(
        primary = KissosColors.Purple.primary,
        onPrimary = KissosColors.White,
        primaryContainer = KissosColors.Purple.light,
        onPrimaryContainer = KissosColors.White,
        inversePrimary = KissosColors.Purple.dark,
        secondary = KissosColors.Green.primary,
        onSecondary = KissosColors.White,
        secondaryContainer = KissosColors.Green.light,
        onSecondaryContainer = KissosColors.White,
        tertiary = KissosColors.Green.primary,
        onTertiary = KissosColors.White,
        tertiaryContainer = KissosColors.Green.light,
        onTertiaryContainer = KissosColors.White,

        error = KissosColors.Red.primary,
        onError = KissosColors.White,
        errorContainer = KissosColors.Red.light,
        onErrorContainer = KissosColors.White,

        background = KissosColors.White,
        onBackground = KissosColors.Black,
        surface = surface,
        onSurface = KissosColors.Black,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = KissosColors.Black,
        surfaceTint = surfaceTint,
        inverseSurface = KissosColors.DarkGray,
        inverseOnSurface = KissosColors.White,

        outline = KissosColors.Gray,
        outlineVariant = KissosColors.DarkGray,
        scrim = KissosColors.ExtraDarkGray.copy(alpha = 0.8f),

        surfaceBright = surface,
        surfaceDim = surface,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface.atElevation(surfaceTint, ElevationLow),
        surfaceContainer = surface.atElevation(surfaceTint, ElevationMedium),
        surfaceContainerHigh = surface.atElevation(surfaceTint, ElevationHigh),
        surfaceContainerHighest = surfaceVariant,
    )
}

private fun kissosDarkColorScheme(isTrueBlack: Boolean): ColorScheme {
    val surface = if (isTrueBlack) KissosColors.TrueBlack else KissosColors.Black
    val surfaceTint = KissosColors.White
    val surfaceVariant = KissosColors.ExtraDarkGray
    return darkColorScheme(
        primary = KissosColors.Purple.primary,
        onPrimary = KissosColors.White,
        primaryContainer = KissosColors.Purple.light,
        onPrimaryContainer = KissosColors.White,
        inversePrimary = KissosColors.Purple.dark,
        secondary = KissosColors.Green.primary,
        onSecondary = KissosColors.White,
        secondaryContainer = KissosColors.Green.light,
        onSecondaryContainer = KissosColors.White,
        tertiary = KissosColors.Green.primary,
        onTertiary = KissosColors.White,
        tertiaryContainer = KissosColors.Green.light,
        onTertiaryContainer = KissosColors.White,

        error = KissosColors.Red.primary,
        onError = KissosColors.White,
        errorContainer = KissosColors.Red.light,
        onErrorContainer = KissosColors.White,

        background = surface,
        onBackground = KissosColors.White,
        surface = surface,
        onSurface = KissosColors.White,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = KissosColors.White,
        surfaceTint = surfaceTint,
        inverseSurface = KissosColors.LightGray,
        inverseOnSurface = surface,

        outline = KissosColors.Gray,
        outlineVariant = KissosColors.LightGray,
        scrim = KissosColors.ExtraLightGray.copy(alpha = 0.8f),

        surfaceBright = surface,
        surfaceDim = surface,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface.atElevation(surfaceTint, ElevationLow),
        surfaceContainer = surface.atElevation(surfaceTint, ElevationMedium),
        surfaceContainerHigh = surface.atElevation(surfaceTint, ElevationHigh),
        surfaceContainerHighest = surfaceVariant,
    )
}

// Material 3 tonal elevation levels 1, 2 and 3, in dp.
private const val ElevationLow = 1f
private const val ElevationMedium = 3f
private const val ElevationHigh = 6f

/**
 * The surface colour Material 3 used to draw at [elevationDp] before the surface container
 * roles existed, so cards, sheets and dialogs keep the look they had on Material3 1.2.
 */
@Suppress("MagicNumber")
private fun Color.atElevation(surfaceTint: Color, elevationDp: Float): Color {
    val alpha = ((4.5f * ln(elevationDp + 1)) + 2f) / 100f
    return surfaceTint.copy(alpha = alpha).compositeOver(this)
}

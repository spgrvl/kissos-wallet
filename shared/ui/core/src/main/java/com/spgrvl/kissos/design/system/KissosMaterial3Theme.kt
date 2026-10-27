package com.spgrvl.kissos.design.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.spgrvl.kissos.design.system.colors.KissosColors

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

private fun kissosLightColorScheme(): ColorScheme = ColorScheme(
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
    surface = KissosColors.White,
    onSurface = KissosColors.Black,
    surfaceVariant = KissosColors.ExtraLightGray,
    onSurfaceVariant = KissosColors.Black,
    surfaceTint = KissosColors.Black,
    inverseSurface = KissosColors.DarkGray,
    inverseOnSurface = KissosColors.White,

    outline = KissosColors.Gray,
    outlineVariant = KissosColors.DarkGray,
    scrim = KissosColors.ExtraDarkGray.copy(alpha = 0.8f)
)

private fun kissosDarkColorScheme(isTrueBlack: Boolean): ColorScheme = ColorScheme(
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

    background = if (isTrueBlack) KissosColors.TrueBlack else KissosColors.Black,
    onBackground = KissosColors.White,
    surface = if (isTrueBlack) KissosColors.TrueBlack else KissosColors.Black,
    onSurface = KissosColors.White,
    surfaceVariant = KissosColors.ExtraDarkGray,
    onSurfaceVariant = KissosColors.White,
    surfaceTint = KissosColors.White,
    inverseSurface = KissosColors.LightGray,
    inverseOnSurface = if (isTrueBlack) KissosColors.TrueBlack else KissosColors.Black,

    outline = KissosColors.Gray,
    outlineVariant = KissosColors.LightGray,
    scrim = KissosColors.ExtraLightGray.copy(alpha = 0.8f)
)

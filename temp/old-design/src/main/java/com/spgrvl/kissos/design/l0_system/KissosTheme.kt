package com.spgrvl.kissos.design.l0_system

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.design.api.KissosDesign
import com.spgrvl.kissos.design.system.KissosMaterial3Theme

@Deprecated("Old design system. Use `:ivy-design` and Material3")
val LocalKissosColors = compositionLocalOf<KissosColors> { error("No KissosColors") }

@Deprecated("Old design system. Use `:ivy-design` and Material3")
val LocalKissosTypography = compositionLocalOf<KissosTypography> { error("No KissosTypography") }

@Deprecated("Old design system. Use `:ivy-design` and Material3")
val LocalKissosShapes = compositionLocalOf<KissosShapes> { error("No KissosShapes") }

@Deprecated("Old design system. Use `:ivy-design` and Material3")
object UI {
    val colors: KissosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKissosColors.current

    val typo: KissosTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalKissosTypography.current

    val shapes: KissosShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalKissosShapes.current
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosTheme(
    theme: Theme,
    design: KissosDesign,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = design.colors(theme, isDarkTheme)
    val typography = design.typography()
    val shapes = design.shapes()

    CompositionLocalProvider(
        LocalKissosColors provides colors,
        LocalKissosTypography provides typography,
        LocalKissosShapes provides shapes
    ) {
        val view = LocalView.current
        if (!view.isInEditMode && view.context is Activity) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    colors.isLight
            }
        }

        KissosMaterial3Theme(
            dark = !colors.isLight,
            isTrueBlack = theme == Theme.AMOLED_DARK,
            content = content,
        )
    }
}

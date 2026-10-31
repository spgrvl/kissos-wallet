package com.spgrvl.kissos.design.api

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.design.KissosContext
import com.spgrvl.kissos.design.l0_system.KissosTheme
import com.spgrvl.kissos.ui.time.TimeFormatter

val LocalKissosContext = compositionLocalOf<KissosContext> { error("No LocalKissosContext") }

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeConverter = compositionLocalOf<TimeConverter> { error("No LocalTimeConverter") }

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeProvider = compositionLocalOf<TimeProvider> { error("No LocalTimeProvider") }

@Suppress("CompositionLocalAllowlist")
@Deprecated("Used only for time migration to Instant. Never use it in new code!")
val LocalTimeFormatter = compositionLocalOf<TimeFormatter> { error("No LocalTimeFormatter") }

@SuppressLint("ComposeModifierMissing")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosUI(
    timeConverter: TimeConverter,
    timeProvider: TimeProvider,
    timeFormatter: TimeFormatter,
    design: KissosDesign,
    includeSurface: Boolean = true,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val kissosContext = design.context()

    CompositionLocalProvider(
        LocalKissosContext provides kissosContext,
        LocalTimeConverter provides timeConverter,
        LocalTimeProvider provides timeProvider,
        LocalTimeFormatter provides timeFormatter,
    ) {
        KissosTheme(
            theme = kissosContext.theme,
            design = design
        ) {
            WrapWithSurface(includeSurface = includeSurface) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    kissosContext.screenWidth = with(LocalDensity.current) {
                        maxWidth.roundToPx()
                    }
                    kissosContext.screenHeight = with(LocalDensity.current) {
                        maxHeight.roundToPx()
                    }

                    content()
                }
            }
        }
    }
}

@Composable
private fun WrapWithSurface(
    includeSurface: Boolean,
    content: @Composable () -> Unit,
) {
    if (includeSurface) {
        Surface {
            content()
        }
    } else {
        content()
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun kissosContext(): KissosContext {
    return LocalKissosContext.current
}

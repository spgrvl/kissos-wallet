package com.spgrvl.kissos.design.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.spgrvl.kissos.base.resource.AndroidResourceProvider
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.time.impl.DeviceTimeProvider
import com.spgrvl.kissos.base.time.impl.StandardTimeConverter
import com.spgrvl.kissos.design.KissosContext
import com.spgrvl.kissos.design.api.KissosDesign
import com.spgrvl.kissos.design.api.KissosUI
import com.spgrvl.kissos.design.api.systems.KissosWalletDesign
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.ui.time.impl.AndroidDevicePreferences
import com.spgrvl.kissos.ui.time.impl.KissosTimeFormatter

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosComponentPreview(
    modifier: Modifier = Modifier,
    design: KissosDesign = defaultDesign(),
    theme: Theme = Theme.LIGHT,
    content: @Composable BoxScope.() -> Unit
) {
    KissosPreview(
        design = design,
        theme = theme
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(UI.colors.pure),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosPreview(
    design: KissosDesign,
    theme: Theme = Theme.LIGHT,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    design.context().switchTheme(theme = theme)
    val timeProvider = DeviceTimeProvider()
    val timeConverter = StandardTimeConverter(timeProvider)
    KissosUI(
        design = design,
        content = content,
        timeConverter = timeConverter,
        timeProvider = timeProvider,
        timeFormatter = KissosTimeFormatter(
            resourceProvider = AndroidResourceProvider(LocalContext.current),
            timeProvider = timeProvider,
            converter = timeConverter,
            devicePreferences = AndroidDevicePreferences(LocalContext.current)
        )
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun defaultDesign(): KissosDesign = object : KissosWalletDesign() {
    override fun context(): KissosContext = object : KissosContext() {
    }
}

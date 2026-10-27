package com.spgrvl.kissos.legacy

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.legacy.appContext
import com.spgrvl.kissos.design.KissosContext
import com.spgrvl.kissos.design.api.KissosDesign
import com.spgrvl.kissos.design.api.kissosContext
import com.spgrvl.kissos.design.api.systems.KissosWalletDesign
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.utils.KissosPreview
import com.spgrvl.kissos.domain.RootScreen
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.navigation.NavigationRoot

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun kissosWalletCtx(): KissosWalletCtx = kissosContext() as KissosWalletCtx

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun rootView(): View = LocalView.current

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun rootActivity(): AppCompatActivity = LocalContext.current as AppCompatActivity

@Composable
fun rootScreen(): RootScreen = LocalContext.current as RootScreen

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosWalletComponentPreview(
    theme: Theme = Theme.LIGHT,
    Content: @Composable BoxScope.() -> Unit
) {
    KissosWalletPreview(
        theme = theme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.pure),
            contentAlignment = Alignment.Center
        ) {
            Content()
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosWalletPreview(
    theme: Theme = Theme.LIGHT,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    appContext = rootView().context
    KissosPreview(
        theme = theme,
        design = appDesign(KissosWalletCtx()),
    ) {
        NavigationRoot(navigation = Navigation()) {
            content()
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun appDesign(context: KissosWalletCtx): KissosDesign = object : KissosWalletDesign() {
    override fun context(): KissosContext = context
}

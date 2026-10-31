package com.spgrvl.kissos.navigation

import androidx.compose.runtime.Composable
import com.spgrvl.kissos.design.system.KissosMaterial3Theme

@Composable
fun KissosPreview(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    NavigationRoot(navigation = Navigation()) {
        KissosMaterial3Theme(dark = dark, isTrueBlack = false, content = content)
    }
}

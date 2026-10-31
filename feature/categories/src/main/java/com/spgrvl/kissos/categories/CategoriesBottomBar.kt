package com.spgrvl.kissos.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.Blue
import com.spgrvl.kissos.wallet.ui.theme.components.BackBottomBar
import com.spgrvl.kissos.wallet.ui.theme.components.KissosButton

@Composable
internal fun BoxWithConstraintsScope.CategoriesBottomBar(
    onClose: () -> Unit,
    onAddCategory: () -> Unit
) {
    BackBottomBar(onBack = onClose) {
        KissosButton(
            text = stringResource(R.string.add_category),
            iconStart = R.drawable.ic_plus
        ) {
            onAddCategory()
        }
    }
}

@Preview
@Composable
private fun PreviewBottomBar() {
    com.spgrvl.kissos.legacy.KissosWalletPreview {
        Column(
            Modifier
                .fillMaxSize()
                .background(Blue)
        ) {
        }

        CategoriesBottomBar(
            onAddCategory = {},
            onClose = {}
        )
    }
}

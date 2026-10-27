package com.spgrvl.kissos.exchangerates

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
internal fun BoxWithConstraintsScope.ExchangeRatesBottomBar(
    onClose: () -> Unit,
    onAddRate: () -> Unit
) {
    BackBottomBar(onBack = onClose) {
        KissosButton(
            text = stringResource(R.string.add_manual_exchange_rate),
            iconStart = R.drawable.ic_plus
        ) {
            onAddRate()
        }
    }
}

@Preview
@Composable
private fun PreviewBottomBar() {
    KissosWalletPreview {
        Column(
            Modifier
                .fillMaxSize()
                .background(Blue)
        ) {
        }

        ExchangeRatesBottomBar(
            onAddRate = {},
            onClose = {}
        )
    }
}

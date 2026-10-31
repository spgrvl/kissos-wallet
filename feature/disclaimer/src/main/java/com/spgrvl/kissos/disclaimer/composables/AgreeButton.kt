package com.spgrvl.kissos.disclaimer.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.spgrvl.kissos.ui.R

@Composable
fun AgreeButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(stringResource(R.string.agreement_accept))
    }
}
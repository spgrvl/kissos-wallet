package com.spgrvl.kissos.disclaimer.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.spgrvl.kissos.ui.R

@Composable
fun AcceptTermsText(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.agreement_intro),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
    )
}
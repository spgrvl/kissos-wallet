package com.spgrvl.kissos.disclaimer.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.spgrvl.kissos.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerTopAppBar(
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(R.string.agreement_title))
        }
    )
}
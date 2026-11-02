package com.spgrvl.kissos.disclaimer.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.system.colors.KissosColors
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.component.KissosPrivacyPolicyUrl

@Composable
fun PrivacyPolicyLink(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Text(
        modifier = modifier
            .clickable { uriHandler.openUri(KissosPrivacyPolicyUrl) }
            .padding(vertical = 8.dp),
        text = stringResource(R.string.privacy_policy),
        style = MaterialTheme.typography.bodyMedium,
        color = KissosColors.Blue.primary,
        textDecoration = TextDecoration.Underline,
    )
}

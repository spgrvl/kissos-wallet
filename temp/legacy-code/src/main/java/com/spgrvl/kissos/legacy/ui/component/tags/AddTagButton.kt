package com.spgrvl.kissos.legacy.ui.component.tags

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.data.model.TagId
import com.spgrvl.kissos.design.l0_system.Orange3
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.wallet.ui.theme.Gradient
import com.spgrvl.kissos.wallet.ui.theme.components.KissosBorderButton
import com.spgrvl.kissos.wallet.ui.theme.components.KissosButton
import com.spgrvl.kissos.wallet.ui.theme.findContrastTextColor
import kotlinx.collections.immutable.ImmutableList
import com.spgrvl.kissos.ui.R

@Composable
fun AddTagButton(
    transactionAssociatedTags: ImmutableList<TagId>,
    onClick: () -> Unit
) {
    if (transactionAssociatedTags.isNotEmpty()) {
        ViewTagsButton(transactionTags = transactionAssociatedTags, onClick = onClick)
    } else {
        AddTagsButton(onClick = onClick)
    }
}

@Composable
private fun ViewTagsButton(
    transactionTags: ImmutableList<TagId>,
    onClick: () -> Unit,
) {
    val contrastColor = findContrastTextColor(Orange3)
    KissosButton(
        modifier = Modifier.padding(start = 24.dp),
        text = if (transactionTags.size <= 1) "${transactionTags.size}\t Tag" else "${transactionTags.size}\t Tags",
        backgroundGradient = Gradient.solid(Orange3),
        textStyle = UI.typo.b2.style(
            color = contrastColor,
            fontWeight = FontWeight.Bold
        ),
        iconTint = contrastColor,
        hasGlow = false,
        iconEnd = R.drawable.ic_onboarding_next_arrow,
        wrapContentMode = true,
        onClick = onClick
    )
}

@Composable
private fun AddTagsButton(
    onClick: () -> Unit,
) {
    KissosBorderButton(
        modifier = Modifier.padding(start = 24.dp),
        iconStart = R.drawable.ic_plus,
        iconTint = UI.colors.pureInverse,
        text = stringResource(R.string.add_tags),
        onClick = onClick
    )
}

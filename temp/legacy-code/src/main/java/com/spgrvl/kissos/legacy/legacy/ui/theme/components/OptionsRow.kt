package com.spgrvl.kissos.legacy.legacy.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.ui.R
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

/** A category or an account to pick. */
@Suppress("DataClassTypedIDs")
@Immutable
data class OptionUi(val id: UUID, val name: String, val color: Int)

/** A labelled row of chips to pick one option, or none. */
@Composable
fun OptionsRow(
    label: String,
    options: ImmutableList<OptionUi>,
    selected: UUID?,
    onSelect: (UUID?) -> Unit,
    modifier: Modifier = Modifier,
): Unit = Column(modifier = modifier) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = label,
        style = UI.typo.b2.style(fontWeight = FontWeight.Bold)
    )
    Spacer(Modifier.height(8.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "none") {
            OptionChip(
                text = stringResource(R.string.none),
                color = UI.colors.mediumInverse,
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(items = options, key = { it.id }) { option ->
            OptionChip(
                text = option.name,
                color = Color(option.color),
                selected = selected == option.id,
                onClick = { onSelect(option.id) },
            )
        }
    }
}

@Composable
private fun OptionChip(
    text: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clip(UI.shapes.rFull)
            .background(if (selected) color else UI.colors.pure, UI.shapes.rFull)
            .border(2.dp, color, UI.shapes.rFull)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        style = UI.typo.b2.style(
            color = if (selected) UI.colors.pure else UI.colors.pureInverse,
            fontWeight = FontWeight.Bold
        )
    )
}

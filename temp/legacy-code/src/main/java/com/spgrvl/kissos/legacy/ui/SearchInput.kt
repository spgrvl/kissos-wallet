package com.spgrvl.kissos.legacy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.Gray
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l1_buildingBlocks.IvyIcon
import com.spgrvl.kissos.legacy.utils.onScreenStart
import com.spgrvl.kissos.legacy.utils.selectEndTextFieldValue
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.components.IvyBasicTextField

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("MagicNumber")
@Composable
fun SearchInput(
    searchQueryTextFieldValue: TextFieldValue,
    hint: String,
    focus: Boolean = true,
    showClearIcon: Boolean = true,
    onSetSearchQueryTextField: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.rFull)
            .background(UI.colors.pure)
            .border(1.dp, Gray, UI.shapes.rFull),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(icon = R.drawable.ic_search, modifier = Modifier.weight(1f))

        val searchFocus = FocusRequester()
        IvyBasicTextField(
            modifier = Modifier
                .weight(5f)
                .padding(vertical = 12.dp)
                .focusRequester(searchFocus),
            value = searchQueryTextFieldValue,
            hint = hint,
            onValueChanged = {
                onSetSearchQueryTextField(it)
            }
        )

        if (focus) {
            onScreenStart {
                searchFocus.requestFocus()
            }
        }

        if (showClearIcon) {
            IvyIcon(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSetSearchQueryTextField(selectEndTextFieldValue(""))
                    },
                icon = R.drawable.ic_outline_clear_24
            )
        }
    }
}

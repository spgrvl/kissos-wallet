package com.spgrvl.kissos.wallet.ui.theme.modal

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.design.utils.thenIf
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.data.model.Month
import com.spgrvl.kissos.legacy.data.model.Month.Companion.monthsList
import com.spgrvl.kissos.legacy.utils.dateNowUTC
import com.spgrvl.kissos.legacy.utils.drawColoredShadow
import com.spgrvl.kissos.legacy.utils.hideKeyboard
import com.spgrvl.kissos.legacy.utils.onScreenStart
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.Gradient
import com.spgrvl.kissos.wallet.ui.theme.Kissos
import com.spgrvl.kissos.wallet.ui.theme.components.WrapContentRow
import com.spgrvl.kissos.wallet.ui.theme.findContrastTextColor
import java.time.LocalDate
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("ParameterNaming")
@Composable
fun BoxWithConstraintsScope.MonthPickerModal(
    initialDate: LocalDate,
    visible: Boolean,
    dismiss: () -> Unit,
    onMonthSelected: (Int) -> Unit,
    id: UUID = UUID.randomUUID(),
) {
    var selectedMonth by remember {
        mutableStateOf(initialDate.monthValue)
    }

    KissosModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSave {
                onMonthSelected(selectedMonth)
                dismiss()
            }
        }
    ) {
        val view = LocalView.current
        onScreenStart {
            hideKeyboard(view)
        }

        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = stringResource(R.string.choose_month)
        )

        Spacer(Modifier.height(24.dp))

        MonthPicker(
            selectedMonth = selectedMonth,
            onMonthSelected = {
                selectedMonth = it
            }
        )

        Spacer(Modifier.height(56.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun MonthPicker(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    val months = monthsList()

    WrapContentRow(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        horizontalMarginBetweenItems = 12.dp,
        verticalMarginBetweenRows = 12.dp,
        items = months
    ) {
        MonthButton(
            month = it,
            selected = it.monthValue == selectedMonth
        ) {
            onMonthSelected(it.monthValue)
        }
    }
}

@Composable
private fun MonthButton(
    month: Month,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val monthColor = Kissos

    val medium = UI.colors.medium
    val rFull = UI.shapes.rFull

    Text(
        modifier = Modifier
            .thenIf(selected) {
                drawColoredShadow(monthColor)
            }
            .clip(UI.shapes.rFull)
            .clickable(onClick = onClick)
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selected) {
                background(
                    brush = Gradient
                        .solid(monthColor)
                        .asHorizontalBrush(),
                    rFull
                )
            }
            .padding(horizontal = 40.dp, vertical = 12.dp),
        text = month.name,
        style = UI.typo.b2.style(
            color = if (selected) {
                findContrastTextColor(monthColor)
            } else {
                UI.colors.pureInverse
            },
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview {
        MonthPickerModal(
            initialDate = dateNowUTC(),
            visible = true,
            dismiss = {},
            onMonthSelected = {}
        )
    }
}

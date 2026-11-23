package com.spgrvl.kissos.legacy.legacy.ui.theme.components

import com.spgrvl.kissos.ui.CurrencyDisplay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.design.utils.thenIf
import com.spgrvl.kissos.legacy.KissosWalletComponentPreview
import com.spgrvl.kissos.legacy.utils.format
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.Green
import com.spgrvl.kissos.wallet.ui.theme.Kissos
import com.spgrvl.kissos.wallet.ui.theme.Orange
import com.spgrvl.kissos.wallet.ui.theme.Red
import com.spgrvl.kissos.wallet.ui.theme.White
import com.spgrvl.kissos.wallet.ui.theme.components.KissosIcon
import com.spgrvl.kissos.wallet.ui.theme.wallet.AmountCurrencyB2Row
import kotlin.math.abs

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BudgetBattery(
    modifier: Modifier = Modifier,
    currency: String,
    expenses: Double,
    budget: Double,
    backgroundNotFilled: Color = UI.colors.pure,
    onClick: (() -> Unit)? = null,
) {
    if (budget == 0.0) return
    val percentSpent = expenses / budget

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .background(backgroundNotFilled)
            .drawBehind {
                drawRect(
                    color = when {
                        percentSpent <= 0.25 -> Green
                        percentSpent <= 0.50 -> Kissos
                        percentSpent <= 0.75 -> Orange
                        else -> Red
                    },
                    size = size.copy(
                        width = (size.width * percentSpent).toFloat()
                    )
                )
            }
            .thenIf(onClick != null) {
                clickable {
                    onClick?.invoke()
                }
            }
    ) {
        BudgetBatteryText(
            modifier = Modifier.overBatteryFill(percentSpent),
            currency = currency,
            expenses = expenses,
            budget = budget,
            colors = BatteryTextColors(text = White, caption = White),
        )
        BudgetBatteryText(
            modifier = Modifier.pastBatteryFill(percentSpent),
            currency = currency,
            expenses = expenses,
            budget = budget,
            colors = BatteryTextColors(text = UI.colors.pureInverse, caption = UI.colors.mediumInverse),
        )
    }
}

@Composable
private fun BudgetBatteryText(
    currency: String,
    expenses: Double,
    budget: Double,
    colors: BatteryTextColors,
    modifier: Modifier = Modifier,
) {
    val exceeded = expenses > budget
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        KissosIcon(
            icon = if (exceeded) R.drawable.ic_buffer_exceeded else R.drawable.ic_buffer_ok,
            tint = colors.text
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = if (exceeded) {
                    stringResource(R.string.budget_exceeded_by)
                } else {
                    stringResource(R.string.left_to_spend)
                },
                style = UI.typo.c.style(
                    color = colors.text,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(4.dp))

            AmountCurrencyB2Row(
                amount = abs(budget - expenses),
                currency = currency,
                textColor = colors.text
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "${CurrencyDisplay.format(expenses.format(currency), currency)}/" +
                    CurrencyDisplay.format(budget.format(currency), currency),
                style = UI.typo.nC.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.caption
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview_budget_0() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                budget = 0.0,
                expenses = 100.45,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_expenses_0() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                budget = 1000.0,
                expenses = 0.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_spent_very_low() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 100000.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_25() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 20000.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_50() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 10000.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_75() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 7500.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_90() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 5500.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_100() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 5000.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_buffer_125() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = 5000.0,
                budget = 2500.0,
                currency = "BGN"
            )
        }
    }
}

@Preview
@Composable
private fun Preview_expenses_negative() {
    KissosWalletComponentPreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UI.colors.medium),
            contentAlignment = Alignment.Center
        ) {
            BudgetBattery(
                modifier = Modifier.padding(horizontal = 32.dp),
                expenses = -348.54,
                budget = 1000.0,
                currency = "BGN"
            )
        }
    }
}

package com.spgrvl.kissos.wallet.ui.theme.wallet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.legacy.utils.format
import com.spgrvl.kissos.legacy.utils.shortenAmount
import com.spgrvl.kissos.legacy.utils.shouldShortAmount
import com.spgrvl.kissos.ui.CurrencyDisplay

@SuppressLint("ComposeModifierMissing")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun AmountCurrencyB2Row(
    amount: Double,
    currency: String,
    amountFontWeight: FontWeight = FontWeight.ExtraBold,
    textColor: Color = UI.colors.pureInverse
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        MoneyTexts(
            amountText = amount.format(currency),
            currency = currency,
            numberStyle = UI.typo.nB2.style(
                fontWeight = amountFontWeight,
                color = textColor
            ),
            symbolStyle = UI.typo.nB2.style(
                fontWeight = FontWeight.Normal,
                color = textColor
            ),
        )
    }
}

@SuppressLint("ComposeModifierMissing")
@Composable
fun AmountCurrencyB1Row(
    amount: Double,
    currency: String,
    amountFontWeight: FontWeight = FontWeight.Bold,
    textColor: Color = UI.colors.pureInverse
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AmountCurrencyB1(
            amount = amount,
            currency = currency,
            amountFontWeight = amountFontWeight,
            textColor = textColor
        )
    }
}

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing"
)
@Composable
fun AmountCurrencyB1(
    amount: Double,
    currency: String,
    amountFontWeight: FontWeight = FontWeight.Bold,
    textColor: Color = UI.colors.pureInverse,
    shortenBigNumbers: Boolean = false
) {
    val shortAmount = shortenBigNumbers && shouldShortAmount(amount)
    val text = if (shortAmount) shortenAmount(amount) else amount.format(currency)
    MoneyTexts(
        amountText = text,
        currency = currency,
        numberModifier = Modifier.testTag("amount_currency_b1"),
        numberStyle = UI.typo.nB1.style(
            fontWeight = amountFontWeight,
            color = textColor
        ),
        symbolStyle = UI.typo.nB1.style(
            fontWeight = FontWeight.Normal,
            color = textColor
        ),
    )
}

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing"
)
@Composable
fun AmountCurrencyH1(
    amount: Double,
    currency: String,
    @SuppressLint("ComposeContentEmitterReturningValues") textColor: Color = UI.colors.pureInverse
) {
    MoneyTexts(
        amountText = amount.format(currency),
        currency = currency,
        numberStyle = UI.typo.nH1.style(
            fontWeight = FontWeight.Bold,
            color = textColor
        ),
        symbolStyle = UI.typo.nH2.style(
            fontWeight = FontWeight.Normal,
            color = textColor
        ),
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
fun AmountCurrencyH2Row(
    amount: Double,
    currency: String,
    textColor: Color = UI.colors.pureInverse
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        MoneyTexts(
            amountText = amount.format(currency),
            currency = currency,
            numberStyle = UI.typo.nH2.style(
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            symbolStyle = UI.typo.b1.style(
                fontWeight = FontWeight.Normal,
                color = textColor
            ),
        )
    }
}

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing"
)
@Composable
fun AmountCurrencyCaption(
    amount: Double,
    currency: String,
    amountFontWeight: FontWeight = FontWeight.ExtraBold,
    textColor: Color = UI.colors.pureInverse
) {
    MoneyTexts(
        amountText = amount.format(currency),
        currency = currency,
        numberStyle = UI.typo.nC.style(
            fontWeight = amountFontWeight,
            color = textColor
        ),
        symbolStyle = UI.typo.nC.style(
            fontWeight = FontWeight.Normal,
            color = textColor
        ),
    )
}

/**
 * The amount and its currency symbol as separate texts ("23,84 €"). Emitted without a
 * container so callers keep their Row.
 */
@SuppressLint(
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing"
)
@Composable
fun MoneyTexts(
    amountText: String,
    currency: String,
    numberStyle: TextStyle,
    symbolStyle: TextStyle,
    numberModifier: Modifier = Modifier,
    symbolSpacing: Dp = 4.dp,
) {
    val parts = CurrencyDisplay.parts(amountText, currency)
    Text(modifier = numberModifier, text = parts.number, style = numberStyle)
    if (parts.suffix.isNotEmpty()) {
        Spacer(modifier = Modifier.width(symbolSpacing))
        Text(text = parts.suffix, style = symbolStyle)
    }
}

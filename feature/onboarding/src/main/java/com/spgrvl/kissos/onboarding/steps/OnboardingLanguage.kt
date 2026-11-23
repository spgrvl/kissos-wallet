package com.spgrvl.kissos.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.utils.setStatusBarDarkTextCompat
import com.spgrvl.kissos.ui.AppLanguage
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.GradientKissos
import com.spgrvl.kissos.wallet.ui.theme.Gray
import com.spgrvl.kissos.wallet.ui.theme.White
import com.spgrvl.kissos.wallet.ui.theme.components.OnboardingButton

/**
 * First onboarding step: the app language. Picking one applies it at once, so the
 * screen switches language and the user sees what they chose.
 */
@Composable
fun OnboardingLanguage(
    selected: AppLanguage?,
    onSelect: (AppLanguage) -> Unit,
    onNext: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    setStatusBarDarkTextCompat(darkText = UI.colors.isLight)

    val context = LocalContext.current
    val shown = selected ?: remember {
        AppLanguage.current(context).takeIf { it != AppLanguage.SYSTEM } ?: AppLanguage.suggested(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(64.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.choose_language),
            style = UI.typo.h2.style(fontWeight = FontWeight.Black)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.choose_language_hint),
            style = UI.typo.nB2.style(fontWeight = FontWeight.Bold, color = Gray)
        )

        Spacer(Modifier.height(40.dp))

        Column(Modifier.selectableGroup()) {
            LanguageOption(
                name = stringResource(R.string.language_greek),
                selected = shown == AppLanguage.GREEK,
                onClick = { onSelect(AppLanguage.GREEK) }
            )
            Spacer(Modifier.height(12.dp))
            LanguageOption(
                name = stringResource(R.string.language_english),
                selected = shown == AppLanguage.ENGLISH,
                onClick = { onSelect(AppLanguage.ENGLISH) }
            )
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(40.dp))

        OnboardingButton(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.next),
            textColor = White,
            backgroundGradient = GradientKissos,
            hasNext = true,
            enabled = true
        ) {
            onNext(shown)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LanguageOption(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = UI.shapes.r3
    Text(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) UI.colors.primary else UI.colors.medium, shape)
            .border(2.dp, if (selected) UI.colors.primary else Color.Transparent, shape)
            .semantics {
                this.selected = selected
                role = Role.RadioButton
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        text = name,
        style = UI.typo.b1.style(
            fontWeight = FontWeight.Bold,
            color = if (selected) White else UI.colors.pureInverse
        )
    )
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview {
        OnboardingLanguage(selected = AppLanguage.GREEK, onSelect = {}, onNext = {})
    }
}

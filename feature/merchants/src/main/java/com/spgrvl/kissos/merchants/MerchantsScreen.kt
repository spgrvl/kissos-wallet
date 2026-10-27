package com.spgrvl.kissos.merchants

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.design.l1_buildingBlocks.ColumnRoot
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.legacy.ui.theme.components.OptionsRow
import com.spgrvl.kissos.navigation.navigation
import com.spgrvl.kissos.navigation.screenScopedViewModel
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.components.BackBottomBar
import com.spgrvl.kissos.wallet.ui.theme.components.KissosNameTextField
import com.spgrvl.kissos.wallet.ui.theme.modal.KissosModal
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalDelete
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalSave
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.MerchantsScreen() {
    val viewModel: MerchantsViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    UI(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: MerchantsState,
    onEvent: (MerchantsEvent) -> Unit,
) {
    val nav = navigation()
    var editing by remember { mutableStateOf<MerchantUi?>(null) }

    ColumnRoot {
        Spacer(Modifier.height(24.dp))
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.merchants),
            style = UI.typo.h2.style(fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(16.dp))

        if (state.merchants.isEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.merchants_empty),
                style = UI.typo.b2.style(color = UI.colors.mediumInverse)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = state.merchants, key = { it.afm }) { merchant ->
                MerchantItem(merchant = merchant, onClick = { editing = merchant })
            }
        }
    }

    BackBottomBar(onBack = { nav.back() }) {}

    EditMerchantModal(
        merchant = editing,
        state = state,
        dismiss = { editing = null },
        onEvent = onEvent,
    )
}

@Composable
private fun MerchantItem(
    merchant: MerchantUi,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.r4)
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = merchant.displayName,
            style = UI.typo.b1.style(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = listOfNotNull(
                stringResource(R.string.merchant_details, merchant.afm),
                merchant.defaults
            ).joinToString(" · "),
            style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.EditMerchantModal(
    merchant: MerchantUi?,
    state: MerchantsState,
    dismiss: () -> Unit,
    onEvent: (MerchantsEvent) -> Unit,
) {
    var name by remember(merchant) { mutableStateOf(TextFieldValue(merchant?.displayName.orEmpty())) }
    var categoryId by remember(merchant) { mutableStateOf(merchant?.category?.value) }
    var accountId by remember(merchant) { mutableStateOf(merchant?.account?.value) }

    KissosModal(
        id = remember { UUID.randomUUID() },
        visible = merchant != null,
        dismiss = dismiss,
        SecondaryActions = {
            ModalDelete {
                merchant?.let { onEvent(MerchantsEvent.Delete(it.afm)) }
                dismiss()
            }
        },
        PrimaryAction = {
            ModalSave(enabled = name.text.isNotBlank()) {
                merchant?.let {
                    onEvent(
                        MerchantsEvent.Save(
                            afm = it.afm,
                            displayName = name.text,
                            category = categoryId?.let(::CategoryId),
                            account = accountId?.let(::AccountId),
                        )
                    )
                }
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = stringResource(R.string.merchant_details, merchant?.afm.orEmpty()))
        Spacer(Modifier.height(24.dp))
        KissosNameTextField(
            modifier = Modifier.padding(horizontal = 32.dp),
            underlineModifier = Modifier.padding(horizontal = 24.dp),
            value = name,
            hint = stringResource(R.string.shop_name),
        ) {
            name = it
        }
        Spacer(Modifier.height(24.dp))
        OptionsRow(
            label = stringResource(R.string.category),
            options = state.categories,
            selected = categoryId,
            onSelect = { categoryId = it },
        )
        Spacer(Modifier.height(16.dp))
        OptionsRow(
            label = stringResource(R.string.account),
            options = state.accounts,
            selected = accountId,
            onSelect = { accountId = it },
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview(Theme.LIGHT) {
        UI(state = previewState, onEvent = {})
    }
}

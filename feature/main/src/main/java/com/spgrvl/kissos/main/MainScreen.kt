package com.spgrvl.kissos.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spgrvl.kissos.accounts.AccountsTab
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.home.HomeTab
import com.spgrvl.kissos.legacy.IvyWalletPreview
import com.spgrvl.kissos.legacy.data.model.MainTab
import com.spgrvl.kissos.legacy.ivyWalletCtx
import com.spgrvl.kissos.legacy.utils.onScreenStart
import com.spgrvl.kissos.navigation.EditPlannedScreen
import com.spgrvl.kissos.navigation.EditTransactionScreen
import com.spgrvl.kissos.navigation.MainScreen
import com.spgrvl.kissos.navigation.navigation
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData
import com.spgrvl.kissos.wallet.ui.theme.modal.edit.AccountModal
import com.spgrvl.kissos.wallet.ui.theme.modal.edit.AccountModalData

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MainScreen(screen: MainScreen) {
    val viewModel: MainViewModel = viewModel()

    val currency by viewModel.currency.observeAsState("")

    onScreenStart {
        viewModel.start(screen)
    }

    val ivyContext = ivyWalletCtx()
    UI(
        screen = screen,
        tab = ivyContext.mainTab,
        baseCurrency = currency,
        selectTab = viewModel::selectTab,
        onCreateAccount = viewModel::createAccount
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: MainScreen,
    tab: MainTab,

    baseCurrency: String,

    selectTab: (MainTab) -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> HomeTab()
        MainTab.ACCOUNTS -> AccountsTab()
    }

    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }

    val nav = navigation()
    BottomBar(
        tab = tab,
        selectTab = selectTab,

        onAddIncome = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.INCOME
                )
            )
        },
        onAddExpense = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE
                )
            )
        },
        onAddTransfer = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.TRANSFER
                )
            )
        },
        onAddPlannedPayment = {
            nav.navigateTo(
                EditPlannedScreen(
                    type = TransactionType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        },
        onScanReceipt = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE,
                    scanReceipt = true,
                )
            )
        },

        showAddAccountModal = {
            accountModalData = AccountModalData(
                account = null,
                balance = 0.0,
                baseCurrency = baseCurrency
            )
        }
    )

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun PreviewMainScreen() {
    IvyWalletPreview {
        UI(
            screen = MainScreen,
            tab = MainTab.HOME,
            baseCurrency = "BGN",
            selectTab = {},
            onCreateAccount = { }
        )
    }
}

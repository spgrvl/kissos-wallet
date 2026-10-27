package com.spgrvl.kissos.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.legacy.TransactionHistoryItem
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.design.api.LocalTimeConverter
import com.spgrvl.kissos.design.api.LocalTimeFormatter
import com.spgrvl.kissos.design.api.LocalTimeProvider
import com.spgrvl.kissos.frp.forward
import com.spgrvl.kissos.frp.then2
import com.spgrvl.kissos.home.Constants.SWIPE_HORIZONTAL_THRESHOLD
import com.spgrvl.kissos.home.customerjourney.CustomerJourney
import com.spgrvl.kissos.home.customerjourney.CustomerJourneyCardModel
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.data.AppBaseData
import com.spgrvl.kissos.legacy.data.BufferInfo
import com.spgrvl.kissos.legacy.data.LegacyDueSection
import com.spgrvl.kissos.legacy.data.model.MainTab
import com.spgrvl.kissos.legacy.data.model.Month
import com.spgrvl.kissos.legacy.data.model.TimePeriod
import com.spgrvl.kissos.legacy.kissosWalletCtx
import com.spgrvl.kissos.legacy.ui.component.transaction.TransactionsDividerLine
import com.spgrvl.kissos.legacy.ui.component.transaction.transactions
import com.spgrvl.kissos.legacy.utils.horizontalSwipeListener
import com.spgrvl.kissos.legacy.utils.rememberSwipeListenerState
import com.spgrvl.kissos.legacy.utils.verticalSwipeListener
import com.spgrvl.kissos.navigation.KissosPreview
import com.spgrvl.kissos.navigation.screenScopedViewModel
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.rememberScrollPositionListState
import com.spgrvl.kissos.wallet.domain.data.KissosCurrency
import com.spgrvl.kissos.wallet.domain.pure.data.IncomeExpensePair
import com.spgrvl.kissos.wallet.ui.theme.modal.BufferModal
import com.spgrvl.kissos.wallet.ui.theme.modal.BufferModalData
import com.spgrvl.kissos.wallet.ui.theme.modal.ChoosePeriodModal
import com.spgrvl.kissos.wallet.ui.theme.modal.ChoosePeriodModalData
import com.spgrvl.kissos.wallet.ui.theme.modal.CurrencyModal
import com.spgrvl.kissos.wallet.ui.theme.modal.DeleteModal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.math.BigDecimal

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.HomeTab() {
    val viewModel: HomeViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    HomeUi(uiState, viewModel::onEvent)
}

@Suppress("LongMethod")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.HomeUi(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val kissosContext = kissosWalletCtx()

    var bufferModalData: BufferModalData? by remember { mutableStateOf(null) }
    var currencyModalVisible by remember { mutableStateOf(false) }
    var choosePeriodModal: ChoosePeriodModalData? by remember {
        mutableStateOf(null)
    }
    var moreMenuExpanded by remember { mutableStateOf(kissosContext.moreMenuExpanded) }
    var skipAllModalVisible by remember { mutableStateOf(false) }
    val setMoreMenuExpanded = { expanded: Boolean ->
        moreMenuExpanded = expanded
        kissosContext.setMoreMenuExpanded(expanded)
    }

    val baseCurrency = uiState.baseData.baseCurrency

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    setMoreMenuExpanded(true)
                }
            )
            .horizontalSwipeListener(
                sensitivity = SWIPE_HORIZONTAL_THRESHOLD,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    kissosContext.selectMainTab(MainTab.ACCOUNTS)
                },
                onSwipeRight = {
                    kissosContext.selectMainTab(MainTab.ACCOUNTS)
                }
            )
    ) {
        val listState = rememberScrollPositionListState(
            key = "home_lazy_column",
            initialFirstVisibleItemIndex = kissosContext.transactionsListState
                ?.firstVisibleItemIndex ?: 0,
            initialFirstVisibleItemScrollOffset = kissosContext.transactionsListState
                ?.firstVisibleItemScrollOffset ?: 0
        )

        HomeHeader(
            expanded = uiState.expanded,
            name = uiState.name,
            period = uiState.period,
            currency = baseCurrency,
            balance = uiState.balance.toDouble(),
            hideBalance = uiState.hideBalance,

            onShowMonthModal = {
                choosePeriodModal = ChoosePeriodModalData(
                    period = uiState.period
                )
            },
            onBalanceClick = {
                onEvent(HomeEvent.BalanceClick)
            },
            onHiddenBalanceClick = {
                onEvent(HomeEvent.HiddenBalanceClick)
            },
            onSelectNextMonth = {
                onEvent(HomeEvent.SelectNextMonth)
            },
            onSelectPreviousMonth = {
                onEvent(HomeEvent.SelectPreviousMonth)
            }
        )

        HomeLazyColumn(
            hideBalance = uiState.hideBalance,
            hideIncome = uiState.hideIncome,
            onSetExpand = {
                onEvent(HomeEvent.SetExpanded(it))
            },
            balance = uiState.balance,
            onOpenMoreMenu = {
                setMoreMenuExpanded(true)
            },
            onBalanceClick = {
                onEvent(HomeEvent.BalanceClick)
            },
            onHiddenBalanceClick = {
                onEvent(HomeEvent.HiddenBalanceClick)
            },
            onHiddenIncomeClick = {
                onEvent(HomeEvent.HiddenIncomeClick)
            },

            period = uiState.period,
            listState = listState,

            baseData = uiState.baseData,

            upcoming = uiState.upcoming,
            overdue = uiState.overdue,

            stats = uiState.stats,
            history = uiState.history,

            customerJourneyCards = uiState.customerJourneyCards,
            shouldShowAccountSpecificColorInTransactions = uiState.shouldShowAccountSpecificColorInTransactions,

            onPayOrGet = forward<Transaction>() then2 {
                HomeEvent.PayOrGetPlanned(it)
            } then2 onEvent,
            onDismiss = forward<CustomerJourneyCardModel>() then2 {
                HomeEvent.DismissCustomerJourneyCard(it)
            } then2 onEvent,
            onSkipTransaction = forward<Transaction>() then2 {
                HomeEvent.SkipPlanned(it)
            } then2 onEvent,
            setUpcomingExpanded = forward<Boolean>() then2 {
                HomeEvent.SetUpcomingExpanded(it)
            } then2 onEvent,
            setOverdueExpanded = forward<Boolean>() then2 {
                HomeEvent.SetOverdueExpanded(it)
            } then2 onEvent,
            onSkipAllTransactions = {
                skipAllModalVisible = true
            }
        )
    }

    MoreMenu(
        expanded = moreMenuExpanded,
        theme = uiState.theme,
        balance = uiState.balance.toDouble(),
        currency = baseCurrency,
        buffer = uiState.buffer.amount.toDouble(),
        onSwitchTheme = {
            onEvent(HomeEvent.SwitchTheme)
        },
        setExpanded = setMoreMenuExpanded,
        onBufferClick = {
            bufferModalData = BufferModalData(
                balance = uiState.balance.toDouble(),
                currency = baseCurrency,
                buffer = uiState.buffer.amount.toDouble()
            )
        },
        onCurrencyClick = {
            currencyModalVisible = true
        }
    )

    BufferModal(
        modal = bufferModalData,
        dismiss = {
            bufferModalData = null
        },
        onBufferChanged = forward<Double>() then2 {
            HomeEvent.SetBuffer(it)
        } then2 onEvent
    )

    CurrencyModal(
        title = stringResource(R.string.set_currency),
        initialCurrency = KissosCurrency.fromCode(baseCurrency),
        visible = currencyModalVisible,
        dismiss = {
            currencyModalVisible = false
        },
        onSetCurrency = forward<String>() then2 {
            HomeEvent.SetCurrency(it)
        } then2 onEvent
    )

    ChoosePeriodModal(
        modal = choosePeriodModal,
        dismiss = {
            choosePeriodModal = null
        },
        onPeriodSelected = forward<TimePeriod>() then2 {
            HomeEvent.SetPeriod(it)
        } then2 onEvent
    )

    DeleteModal(
        visible = skipAllModalVisible,
        title = stringResource(R.string.confirm_skip_all),
        description = stringResource(R.string.confirm_skip_all_description),
        dismiss = {
            skipAllModalVisible = false
        }
    ) {
        onEvent(HomeEvent.SkipAllPlanned(uiState.overdue.trns))
        skipAllModalVisible = false
    }
}

@Suppress("LongParameterList")
@ExperimentalAnimationApi
@Composable
fun HomeLazyColumn(
    hideBalance: Boolean,
    hideIncome: Boolean,
    onSetExpand: (Boolean) -> Unit,
    listState: LazyListState,
    period: TimePeriod,

    baseData: AppBaseData,
    shouldShowAccountSpecificColorInTransactions: Boolean,

    upcoming: LegacyDueSection,
    overdue: LegacyDueSection,
    balance: BigDecimal,
    stats: IncomeExpensePair,
    history: ImmutableList<TransactionHistoryItem>,

    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,

    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,

    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,

    onPayOrGet: (Transaction) -> Unit,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
    onHiddenBalanceClick: () -> Unit,
    onHiddenIncomeClick: () -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
    onSkipAllTransactions: (List<Transaction>) -> Unit,
    modifier: Modifier = Modifier
) {
    val kissosContext = kissosWalletCtx()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                onSetExpand(listState.firstVisibleItemScrollOffset == 0)
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    val timeProvider = LocalTimeProvider.current
    val timeConverter = LocalTimeConverter.current
    val timeFormatter = LocalTimeFormatter.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .testTag("home_lazy_column"),
        state = listState
    ) {
        item {
            CashFlowInfo(
                currency = baseData.baseCurrency,
                balance = balance.toDouble(),

                hideBalance = hideBalance,

                monthlyIncome = stats.income.toDouble(),
                monthlyExpenses = stats.expense.toDouble(),

                onOpenMoreMenu = onOpenMoreMenu,
                onBalanceClick = onBalanceClick,
                onHiddenBalanceClick = onHiddenBalanceClick,
                percentExpanded = 1f,
                hideIncome = hideIncome,
                onHiddenIncomeClick = onHiddenIncomeClick
            )
        }
        item {
            Spacer(Modifier.height(16.dp))

            TransactionsDividerLine()
        }

        item {
            CustomerJourney(
                customerJourneyCards = customerJourneyCards,
                onDismiss = onDismiss
            )
        }

        transactions(
            baseData = baseData,
            upcoming = upcoming,
            setUpcomingExpanded = setUpcomingExpanded,
            overdue = overdue,
            setOverdueExpanded = setOverdueExpanded,
            history = history,
            onPayOrGet = onPayOrGet,
            emptyStateTitle = stringRes(R.string.no_transactions),
            emptyStateText = stringRes(
                R.string.no_transactions_description,
                period.toDisplayLong(
                    startDateOfMonth = kissosContext.startDayOfMonth,
                    timeProvider = timeProvider,
                    timeConverter = timeConverter,
                    timeFormatter = timeFormatter,
                )
            ),
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onSkipTransaction = onSkipTransaction,
            onSkipAllTransactions = onSkipAllTransactions
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun BoxWithConstraintsScope.PreviewHomeTab(isDark: Boolean = false) {
    KissosPreview(isDark) {
        HomeUi(
            uiState = HomeState(
                theme = Theme.AUTO,
                name = "",
                baseData = AppBaseData(
                    baseCurrency = "",
                    accounts = persistentListOf(),
                    categories = persistentListOf()
                ),
                balance = BigDecimal.ZERO,
                buffer = BufferInfo(
                    amount = BigDecimal.ZERO,
                    bufferDiff = BigDecimal.ZERO,
                ),
                customerJourneyCards = persistentListOf(),
                history = persistentListOf(),
                stats = IncomeExpensePair.zero(),
                upcoming = LegacyDueSection(
                    trns = persistentListOf(),
                    stats = IncomeExpensePair.zero(),
                    expanded = false,
                ),
                overdue = LegacyDueSection(
                    trns = persistentListOf(),
                    stats = IncomeExpensePair.zero(),
                    expanded = false,
                ),
                period = TimePeriod(month = Month.monthsList().first(), year = 2023),
                hideBalance = false,
                hideIncome = false,
                expanded = false,
                shouldShowAccountSpecificColorInTransactions = false
            ),
            onEvent = {}
        )
    }
}

/** For screenshot testing */
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    KissosWalletPreview(theme) {
        PreviewHomeTab(isDark)
    }
}

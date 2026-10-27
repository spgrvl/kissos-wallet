package com.spgrvl.kissos

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.spgrvl.kissos.attributions.AttributionsScreenImpl
import com.spgrvl.kissos.balance.BalanceScreen
import com.spgrvl.kissos.budgets.BudgetScreen
import com.spgrvl.kissos.categories.CategoriesScreen
import com.spgrvl.kissos.contributors.ContributorsScreenImpl
import com.spgrvl.kissos.disclaimer.DisclaimerScreenImpl
import com.spgrvl.kissos.exchangerates.ExchangeRatesScreen
import com.spgrvl.kissos.features.FeaturesScreenImpl
import com.spgrvl.kissos.importdata.csv.CSVScreen
import com.spgrvl.kissos.importdata.csvimport.ImportCSVScreen
import com.spgrvl.kissos.loans.loan.LoansScreen
import com.spgrvl.kissos.loans.loandetails.LoanDetailsScreen
import com.spgrvl.kissos.main.MainScreen
import com.spgrvl.kissos.merchants.MerchantsScreen
import com.spgrvl.kissos.navigation.AttributionsScreen
import com.spgrvl.kissos.navigation.BalanceScreen
import com.spgrvl.kissos.navigation.BudgetScreen
import com.spgrvl.kissos.navigation.CSVScreen
import com.spgrvl.kissos.navigation.CategoriesScreen
import com.spgrvl.kissos.navigation.ContributorsScreen
import com.spgrvl.kissos.navigation.DisclaimerScreen
import com.spgrvl.kissos.navigation.EditPlannedScreen
import com.spgrvl.kissos.navigation.EditTransactionScreen
import com.spgrvl.kissos.navigation.ExchangeRatesScreen
import com.spgrvl.kissos.navigation.FeaturesScreen
import com.spgrvl.kissos.navigation.ImportScreen
import com.spgrvl.kissos.navigation.LoanDetailsScreen
import com.spgrvl.kissos.navigation.LoansScreen
import com.spgrvl.kissos.navigation.MainScreen
import com.spgrvl.kissos.navigation.MerchantsScreen
import com.spgrvl.kissos.navigation.OnboardingScreen
import com.spgrvl.kissos.navigation.PieChartStatisticScreen
import com.spgrvl.kissos.navigation.PlannedPaymentsScreen
import com.spgrvl.kissos.navigation.ReleasesScreen
import com.spgrvl.kissos.navigation.ReportScreen
import com.spgrvl.kissos.navigation.Screen
import com.spgrvl.kissos.navigation.SearchScreen
import com.spgrvl.kissos.navigation.SettingsScreen
import com.spgrvl.kissos.navigation.TransactionsScreen
import com.spgrvl.kissos.onboarding.OnboardingScreen
import com.spgrvl.kissos.piechart.PieChartStatisticScreen
import com.spgrvl.kissos.planned.edit.EditPlannedScreen
import com.spgrvl.kissos.planned.list.PlannedPaymentsScreen
import com.spgrvl.kissos.releases.ReleasesScreenImpl
import com.spgrvl.kissos.reports.ReportScreen
import com.spgrvl.kissos.search.SearchScreen
import com.spgrvl.kissos.settings.SettingsScreen
import com.spgrvl.kissos.transaction.EditTransactionScreen
import com.spgrvl.kissos.transactions.TransactionsScreen

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
@Suppress("CyclomaticComplexMethod", "FunctionNaming")
fun BoxWithConstraintsScope.KissosNavGraph(screen: Screen?) {
    when (screen) {
        null -> {
            // show nothing
        }

        is MainScreen -> MainScreen(screen = screen)
        is OnboardingScreen -> OnboardingScreen(screen = screen)
        is ExchangeRatesScreen -> ExchangeRatesScreen()
        is MerchantsScreen -> MerchantsScreen()
        is EditTransactionScreen -> EditTransactionScreen(screen = screen)
        is TransactionsScreen -> TransactionsScreen(screen = screen)
        is PieChartStatisticScreen -> PieChartStatisticScreen(screen = screen)
        is CategoriesScreen -> CategoriesScreen(screen = screen)
        is SettingsScreen -> SettingsScreen()
        is PlannedPaymentsScreen -> PlannedPaymentsScreen(screen = screen)
        is EditPlannedScreen -> EditPlannedScreen(screen = screen)
        is BalanceScreen -> BalanceScreen(screen = screen)
        is ImportScreen -> ImportCSVScreen(screen = screen)
        is ReportScreen -> ReportScreen(screen = screen)
        is BudgetScreen -> BudgetScreen(screen = screen)
        is LoansScreen -> LoansScreen(screen = screen)
        is LoanDetailsScreen -> LoanDetailsScreen(screen = screen)
        is SearchScreen -> SearchScreen(screen = screen)
        is CSVScreen -> CSVScreen(screen = screen)
        FeaturesScreen -> FeaturesScreenImpl()
        AttributionsScreen -> AttributionsScreenImpl()
        ContributorsScreen -> ContributorsScreenImpl()
        ReleasesScreen -> ReleasesScreenImpl()
        DisclaimerScreen -> DisclaimerScreenImpl()
    }
}

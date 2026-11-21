package com.spgrvl.kissos.home.customerjourney

import com.spgrvl.kissos.base.legacy.SharedPrefs
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.db.dao.read.PlannedPaymentRuleDao
import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.design.l0_system.Gradient
import com.spgrvl.kissos.design.l0_system.Green
import com.spgrvl.kissos.design.l0_system.GreenLight
import com.spgrvl.kissos.design.l0_system.Kissos
import com.spgrvl.kissos.design.l0_system.Orange
import com.spgrvl.kissos.design.l0_system.Red
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.legacy.data.model.MainTab
import com.spgrvl.kissos.navigation.DetectedPaymentsScreen
import com.spgrvl.kissos.navigation.EditPlannedScreen
import com.spgrvl.kissos.navigation.PieChartStatisticScreen
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.widget.transaction.AddTransactionWidgetCompact
import javax.inject.Inject

@Deprecated("Legacy code")
class CustomerJourneyCardsProvider @Inject constructor(
  private val transactionRepository: TransactionRepository,
  private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
  private val sharedPrefs: SharedPrefs,
  private val kissosContext: KissosWalletCtx,
  private val timeProvider: TimeProvider,
  private val bankNotificationDao: BankNotificationDao,
) {

  suspend fun loadCards(): List<CustomerJourneyCardModel> {
    val trnCount = transactionRepository.countHappenedTransactions().value
    val plannedPaymentsCount = plannedPaymentRuleDao.countPlannedPayments()
    val deps = CustomerJourneyDeps(
      timeProvider = timeProvider,
    )

    val waitingPayments = bankNotificationDao.countPayments(DetectedPaymentStatus.NEW)
    return listOfNotNull(detectedPaymentsCard(waitingPayments)) + ACTIVE_CARDS
      .filter {
        it.condition(
          trnCount,
          plannedPaymentsCount,
          kissosContext,
          deps
        ) && !isCardDismissed(it)
      }
  }

  private fun isCardDismissed(cardData: CustomerJourneyCardModel): Boolean {
    return sharedPrefs.getBoolean(sharedPrefsKey(cardData), false)
  }

  fun dismissCard(cardData: CustomerJourneyCardModel) {
    sharedPrefs.putBoolean(sharedPrefsKey(cardData), true)
  }

  private fun sharedPrefsKey(cardData: CustomerJourneyCardModel): String {
    return "${cardData.id}${SharedPrefs._CARD_DISMISSED}"
  }

  companion object {
    /** Payments from bank notifications waiting for the user; it goes away once there are none. */
    private fun detectedPaymentsCard(waiting: Long): CustomerJourneyCardModel? = waiting.takeIf { it > 0 }?.let {
      CustomerJourneyCardModel(
        id = "detected_payments",
        condition = { _, _, _, _ -> true },
        title = stringRes(R.string.detected_payments),
        description = stringRes(R.string.detected_payments_waiting, it.toString()),
        cta = stringRes(R.string.detected_payments_review),
        ctaIcon = R.drawable.ic_notification_m,
        background = Gradient.solid(Kissos),
        hasDismiss = false,
        onAction = { navigation, _, _ ->
          navigation.navigateTo(DetectedPaymentsScreen)
        }
      )
    }

    val ACTIVE_CARDS = listOf(
      adjustBalanceCard(),
      addPlannedPaymentCard(),
      didYouKnow_pinAddTransactionWidgetCard(),
      didYouKnow_expensesPieChart(),
    )

    fun adjustBalanceCard() = CustomerJourneyCardModel(
      id = "adjust_balance",
      condition = { trnCount, _, _, _ ->
        trnCount == 0L
      },
      title = stringRes(R.string.adjust_initial_balance),
      description = stringRes(R.string.adjust_initial_balance_description),
      cta = stringRes(R.string.to_accounts),
      ctaIcon = R.drawable.ic_custom_account_s,
      background = Gradient.solid(Kissos),
      hasDismiss = false,
      onAction = { _, kissosContext, _ ->
        kissosContext.selectMainTab(MainTab.ACCOUNTS)
      }
    )

    fun addPlannedPaymentCard() = CustomerJourneyCardModel(
      id = "add_planned_payment",
      condition = { trnCount, plannedPaymentCount, _, _ ->
        trnCount >= 1 && plannedPaymentCount == 0L
      },
      title = stringRes(R.string.create_first_planned_payment),
      description = stringRes(R.string.create_first_planned_payment_description),
      cta = stringRes(R.string.add_planned_payment),
      ctaIcon = R.drawable.ic_planned_payments,
      background = Gradient.solid(Orange),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(
          EditPlannedScreen(
            type = TransactionType.EXPENSE,
            plannedPaymentRuleId = null
          )
        )
      }
    )

    fun didYouKnow_pinAddTransactionWidgetCard() = CustomerJourneyCardModel(
      id = "add_transaction_widget",
      condition = { trnCount, _, _, _ ->
        trnCount >= 3
      },
      title = stringRes(R.string.did_you_know),
      description = stringRes(R.string.widget_description),
      cta = stringRes(R.string.add_widget),
      ctaIcon = R.drawable.ic_custom_atom_s,
      background = Gradient.solid(GreenLight),
      hasDismiss = true,
      onAction = { _, _, kissosActivity ->
        kissosActivity.pinWidget(AddTransactionWidgetCompact::class.java)
      }
    )

    fun didYouKnow_expensesPieChart() = CustomerJourneyCardModel(
      id = "expenses_pie_chart",
      condition = { trnCount, _, _, _ ->
        trnCount >= 7
      },
      title = stringRes(R.string.did_you_know),
      description = stringRes(R.string.you_can_see_a_piechart),
      cta = stringRes(R.string.expenses_piechart),
      ctaIcon = R.drawable.ic_custom_bills_s,
      background = Gradient.solid(Red),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(PieChartStatisticScreen(type = TransactionType.EXPENSE))
      }
    )

    fun rateUsCard() = CustomerJourneyCardModel(
      id = "rate_us",
      condition = { trnCount, _, _, _ ->
        trnCount >= 10
      },
      title = stringRes(R.string.review_kissos),
      description = stringRes(R.string.review_kissos_description),
      cta = stringRes(R.string.rate_us_on_google_play),
      ctaIcon = R.drawable.ic_custom_star_s,
      background = Gradient.solid(Green),
      hasDismiss = true,
      onAction = { _, _, kissosActivity ->
        kissosActivity.reviewKissosWallet(dismissReviewCard = true)
      }
    )
  }
}

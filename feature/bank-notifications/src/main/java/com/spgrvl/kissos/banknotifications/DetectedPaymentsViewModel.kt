package com.spgrvl.kissos.banknotifications

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.data.db.dao.write.BankNotificationDao
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.domain.banknotification.DetectedPaymentNotifications
import com.spgrvl.kissos.domain.banknotification.DetectedPayments
import com.spgrvl.kissos.navigation.EditTransactionScreen
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import javax.inject.Inject

@Immutable
data class DetectedPaymentsState(
    val waiting: ImmutableList<DetectedPaymentUi>,
    /** What happened to the latest ones, so the user can see what Kissos did. */
    val recent: ImmutableList<DetectedPaymentUi>,
)

@Immutable
@Suppress("DataClassTypedIDs")
data class DetectedPaymentUi(
    val id: UUID,
    /** E.g. "€24.62 · Test Pharmacy" */
    val title: String,
    /** E.g. "Revolut · 21 Nov 2026, 14:02" */
    val details: String,
    val status: DetectedPaymentStatus,
    val canAddDirectly: Boolean,
    /** For an account debit matched to an earlier payment: when that payment was, e.g. "21 Nov 2026, 14:47". */
    val matchedPaymentTime: String?,
    /** What Kissos read from the notification, for a report to support. Not translated. */
    val reportDetails: String,
)

@Suppress("DataClassTypedIDs")
sealed interface DetectedPaymentsEvent {
    data class Add(val id: UUID) : DetectedPaymentsEvent
    data class Edit(val id: UUID) : DetectedPaymentsEvent
    data class Dismiss(val id: UUID) : DetectedPaymentsEvent
    data class Undo(val id: UUID) : DetectedPaymentsEvent
}

@Stable
@HiltViewModel
class DetectedPaymentsViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val dao: BankNotificationDao,
    private val payments: DetectedPayments,
    private val nav: Navigation,
    private val timeConverter: TimeConverter,
) : ComposeViewModel<DetectedPaymentsState, DetectedPaymentsEvent>() {
    private val dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    /** One query for both lists, so a payment that's just been handled is never in both at once. */
    private val lists = dao.observeAllPayments().map { all ->
        val (waiting, handled) = all.partition { it.status == DetectedPaymentStatus.NEW }
        DetectedPaymentsState(
            waiting = waiting.map { toUi(it) }.toImmutableList(),
            recent = handled.take(RECENT_LIMIT).map { toUi(it) }.toImmutableList(),
        )
    }

    @Composable
    override fun uiState(): DetectedPaymentsState {
        val state by lists.collectAsState(DetectedPaymentsState(persistentListOf(), persistentListOf()))
        return state
    }

    override fun onEvent(event: DetectedPaymentsEvent) {
        viewModelScope.launch {
            when (event) {
                is DetectedPaymentsEvent.Add -> if (payments.add(event.id)) cancelNotification(event.id)
                is DetectedPaymentsEvent.Edit -> nav.navigateTo(
                    EditTransactionScreen(
                        initialTransactionId = null,
                        type = TransactionType.EXPENSE,
                        detectedPaymentId = event.id,
                    )
                )

                is DetectedPaymentsEvent.Dismiss -> {
                    payments.dismiss(event.id)
                    cancelNotification(event.id)
                }

                is DetectedPaymentsEvent.Undo -> payments.undo(event.id)
            }
        }
    }

    private fun cancelNotification(id: UUID): Unit = DetectedPaymentNotifications.cancel(context, id)

    private suspend fun toUi(payment: DetectedPaymentEntity): DetectedPaymentUi {
        val draft = payments.draft(payment)
        val app = appLabel(payment.sourcePackage)
        return DetectedPaymentUi(
            id = payment.id,
            title = listOfNotNull(DetectedPaymentNotifier.money(payment), draft.title).joinToString(" · "),
            details = listOf(app, time(payment)).joinToString(" · "),
            status = payment.status,
            canAddDirectly = draft.canAddDirectly,
            matchedPaymentTime = payment.matchedPaymentId?.let { dao.findPayment(it) }?.let(::time),
            reportDetails = listOf(
                "$app (${payment.sourcePackage}) · ${payment.postedAt}",
                "${payment.amount} ${payment.currency} · ${payment.payee ?: "-"} · ${payment.direction}",
                "${payment.kind} · ${payment.status}",
            ).joinToString("\n"),
        )
    }

    private fun time(payment: DetectedPaymentEntity): String =
        with(timeConverter) { payment.postedAt.toLocalDateTime() }.format(dateFormat)

    private fun appLabel(packageName: String): String = try {
        val packageManager = context.packageManager
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }

    private companion object {
        const val RECENT_LIMIT = 30
    }
}

internal val previewPaymentsState = DetectedPaymentsState(
    waiting = persistentListOf(
        DetectedPaymentUi(
            UUID.randomUUID(),
            "€24.62 · Test Pharmacy",
            "Revolut · 21 Nov 2026, 14:02",
            DetectedPaymentStatus.NEW,
            true,
            null,
            "",
        ),
    ),
    recent = persistentListOf(
        DetectedPaymentUi(
            UUID.randomUUID(),
            "€8.80 · Pizza Test",
            "Magenta Pay · 20 Nov 2026, 21:02",
            DetectedPaymentStatus.ADDED,
            false,
            null,
            "",
        ),
    ),
)

package com.spgrvl.kissos.banknotifications

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.design.l1_buildingBlocks.ColumnRoot
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.navigation.navigation
import com.spgrvl.kissos.navigation.screenScopedViewModel
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.androidVersion
import com.spgrvl.kissos.ui.kissosVersion
import com.spgrvl.kissos.wallet.ui.theme.components.BackBottomBar

@Composable
fun BoxWithConstraintsScope.DetectedPaymentsScreen() {
    val viewModel: DetectedPaymentsViewModel = screenScopedViewModel()
    UI(state = viewModel.uiState(), onEvent = viewModel::onEvent)
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: DetectedPaymentsState,
    onEvent: (DetectedPaymentsEvent) -> Unit,
) {
    val nav = navigation()
    val context = LocalContext.current
    var reporting by remember { mutableStateOf<DetectedPaymentUi?>(null) }

    ColumnRoot {
        LazyColumn(
            contentPadding = PaddingValues(top = 24.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.detected_payments),
                    style = UI.typo.h2.style(fontWeight = FontWeight.ExtraBold)
                )
                Spacer(Modifier.height(8.dp))
                if (state.waiting.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.detected_payments_none_waiting),
                        style = UI.typo.b2.style(color = UI.colors.mediumInverse)
                    )
                }
                if (state.waiting.isNotEmpty() || state.recent.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        text = stringResource(R.string.detected_payments_report_hint),
                        style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
                    )
                }
            }
            items(items = state.waiting, key = { "waiting-${it.id}" }) { payment ->
                Payment(payment, onClick = { reporting = payment }) {
                    if (payment.canAddDirectly) {
                        Action(stringResource(R.string.add), UI.colors.green) {
                            onEvent(DetectedPaymentsEvent.Add(payment.id))
                        }
                    }
                    Action(stringResource(R.string.edit), UI.colors.pureInverse) {
                        onEvent(DetectedPaymentsEvent.Edit(payment.id))
                    }
                    Action(stringResource(R.string.dismiss), UI.colors.red) {
                        onEvent(DetectedPaymentsEvent.Dismiss(payment.id))
                    }
                }
            }

            if (state.recent.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                        text = stringResource(R.string.detected_payments_recent),
                        style = UI.typo.b1.style(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
            items(items = state.recent, key = { "recent-${it.id}" }) { payment ->
                Payment(payment, onClick = { reporting = payment }) {
                    Text(
                        // Leaves room for Undo when the text is long
                        modifier = Modifier.weight(1f, fill = false).padding(end = 16.dp),
                        text = when (payment.status) {
                            DetectedPaymentStatus.ADDED -> stringResource(R.string.detected_payment_added)
                            // An account debit for a payment already detected (e.g. Eurobank's, days later)
                            DetectedPaymentStatus.MATCHED ->
                                payment.matchedPaymentTime
                                    ?.let { stringResource(R.string.detected_payment_matched, it) }
                                    ?: stringResource(R.string.detected_payment_matched_earlier)
                            else -> stringResource(R.string.detected_payment_dismissed)
                        },
                        style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
                    )
                    Action(stringResource(R.string.undo), UI.colors.pureInverse) {
                        onEvent(DetectedPaymentsEvent.Undo(payment.id))
                    }
                }
            }
        }
    }

    BackBottomBar(onBack = { nav.back() }) {}

    ReportModal(
        visible = reporting != null,
        title = stringResource(R.string.detected_payment_report_title),
        text = stringResource(R.string.detected_payment_report_confirm),
        dismiss = { reporting = null },
    ) {
        reporting?.let { report(context, it) }
        reporting = null
    }
}

/** Emails what Kissos read to support, or copies it when there's no email app. */
private fun report(context: Context, payment: DetectedPaymentUi) {
    context.sendReport(
        subject = context.getString(R.string.detected_payment_report_subject),
        body = context.getString(
            R.string.detected_payment_report_body,
            payment.reportDetails,
            context.kissosVersion,
            androidVersion,
        ),
        copied = payment.reportDetails,
        noEmail = R.string.detected_payment_report_no_email,
    )
}

@Composable
private fun Payment(
    payment: DetectedPaymentUi,
    onClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.r4)
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 4.dp, top = 16.dp, bottom = 4.dp)
    ) {
        Text(text = payment.title, style = UI.typo.b1.style(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text(
            text = payment.details,
            style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
        )
        Row(verticalAlignment = Alignment.CenterVertically) { actions() }
    }
}

@Composable
private fun Action(text: String, color: Color, onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clip(UI.shapes.r4)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        text = text,
        style = UI.typo.b2.style(color = color, fontWeight = FontWeight.Bold)
    )
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview(Theme.LIGHT) {
        UI(state = previewPaymentsState, onEvent = {})
    }
}

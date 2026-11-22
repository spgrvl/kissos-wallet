package com.spgrvl.kissos.banknotifications

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.design.l1_buildingBlocks.ColumnRoot
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.legacy.ui.theme.components.OptionsRow
import com.spgrvl.kissos.navigation.DetectedPaymentsScreen
import com.spgrvl.kissos.navigation.navigation
import com.spgrvl.kissos.navigation.screenScopedViewModel
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.SupportEmail
import com.spgrvl.kissos.ui.androidVersion
import com.spgrvl.kissos.ui.emailSupport
import com.spgrvl.kissos.ui.kissosVersion
import com.spgrvl.kissos.wallet.ui.theme.components.BackBottomBar
import com.spgrvl.kissos.wallet.ui.theme.components.KissosSwitch
import com.spgrvl.kissos.wallet.ui.theme.modal.KissosModal
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalDelete
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalPositiveButton
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalSave
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.BankNotificationsScreen() {
    val viewModel: BankNotificationsViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(BankNotificationsEvent.Resumed)
        onPauseOrDispose {}
    }

    UI(state = state, onEvent = viewModel::onEvent)
}

@Composable
@Suppress("LongMethod")
private fun BoxWithConstraintsScope.UI(
    state: BankNotificationsState,
    onEvent: (BankNotificationsEvent) -> Unit,
) {
    val nav = navigation()
    val context = LocalContext.current
    var disclosureVisible by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BankAppUi?>(null) }
    var pickingApp by remember { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Kissos's own notifications are optional: payments also wait in the review list
        openAccessSettings(context)
    }

    ColumnRoot {
        LazyColumn(
            contentPadding = PaddingValues(top = 24.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.bank_notifications),
                    style = UI.typo.h2.style(fontWeight = FontWeight.ExtraBold)
                )
                Spacer(Modifier.height(8.dp))
                Description(stringResource(R.string.bank_notifications_description))
                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    text = stringResource(R.string.bank_notifications_read),
                    checked = state.enabled,
                ) { checked ->
                    if (checked) disclosureVisible = true else onEvent(BankNotificationsEvent.SetEnabled(false))
                }
            }

            if (state.enabled) {
                item {
                    if (state.accessGranted) {
                        Box(
                            title = stringResource(R.string.bank_notifications_access_on),
                            subtitle = stringResource(R.string.bank_notifications_access_manage),
                        ) { openAccessSettings(context) }
                    } else {
                        Box(
                            title = stringResource(R.string.bank_notifications_access_off),
                            subtitle = stringResource(R.string.bank_notifications_access_grant),
                            highlighted = true,
                        ) { openAccessSettings(context) }
                    }
                }
            }

            item {
                Box(
                    title = stringResource(R.string.detected_payments),
                    subtitle = if (state.waitingCount > 0) {
                        stringResource(R.string.detected_payments_waiting, state.waitingCount)
                    } else {
                        stringResource(R.string.detected_payments_none_waiting)
                    },
                    highlighted = state.waitingCount > 0,
                ) { nav.navigateTo(DetectedPaymentsScreen) }
            }

            item { Header(stringResource(R.string.bank_notifications_apps)) }
            items(items = state.apps, key = { it.packageName }) { app ->
                Box(title = app.label, subtitle = appSummary(app)) { editing = app }
            }
            item {
                Box(title = stringResource(R.string.bank_notifications_add_app), subtitle = null) { pickingApp = true }
            }

            item {
                Header(stringResource(R.string.bank_notifications_unrecognised))
                Description(stringResource(R.string.bank_notifications_unrecognised_description))
                SwitchRow(
                    text = stringResource(R.string.bank_notifications_keep_unrecognised),
                    checked = state.keepUnparsed,
                ) { onEvent(BankNotificationsEvent.SetKeepUnparsed(it)) }
            }
            items(items = state.unparsed, key = { it.id }) { unparsed ->
                Box(title = unparsed.app, subtitle = unparsed.text) { report(context, unparsed) }
            }
        }
    }

    BackBottomBar(onBack = { nav.back() }) {}

    DisclosureModal(
        visible = disclosureVisible,
        foundApps = state.foundApps,
        dismiss = { disclosureVisible = false },
    ) {
        disclosureVisible = false
        onEvent(BankNotificationsEvent.SetEnabled(true))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openAccessSettings(context)
        }
    }
    EditAppModal(app = editing, state = state, dismiss = { editing = null }, onEvent = onEvent)
    PickAppModal(
        visible = pickingApp,
        apps = state.installedApps,
        dismiss = { pickingApp = false },
    ) {
        pickingApp = false
        onEvent(BankNotificationsEvent.AddApp(it))
    }
}

@Composable
private fun appSummary(app: BankAppUi): String = listOfNotNull(
    app.accountName ?: stringResource(R.string.bank_notifications_no_account),
    stringResource(R.string.bank_notifications_adds_automatically).takeIf { app.autoAdd },
    stringResource(R.string.bank_notifications_not_supported).takeUnless { app.supported },
).joinToString(" · ")

@Composable
private fun Header(text: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
        text = text,
        style = UI.typo.b1.style(fontWeight = FontWeight.ExtraBold)
    )
}

@Composable
private fun Description(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        text = text,
        style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
    )
}

@Composable
private fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = UI.typo.b2.style(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.width(12.dp))
        KissosSwitch(enabled = checked, onEnabledChange = onChange)
    }
}

@Composable
private fun Box(
    title: String,
    subtitle: String?,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.r4)
            .border(2.dp, if (highlighted) UI.colors.primary else UI.colors.medium, UI.shapes.r4)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = title, style = UI.typo.b1.style(fontWeight = FontWeight.Bold))
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
            )
        }
    }
}

/** What turning the feature on means, before Kissos asks the system for access to notifications. */
@Composable
private fun BoxWithConstraintsScope.DisclosureModal(
    visible: Boolean,
    foundApps: List<String>,
    dismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    KissosModal(
        id = remember { UUID.randomUUID() },
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalPositiveButton(
                text = stringResource(R.string.bank_notifications_continue),
                iconStart = R.drawable.ic_check,
                onClick = onContinue,
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = stringResource(R.string.bank_notifications_disclosure_title))
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.bank_notifications_disclosure),
            style = UI.typo.nB2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Normal)
        )
        if (foundApps.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.bank_notifications_found_apps, foundApps.joinToString(", ")),
                style = UI.typo.nB2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun BoxWithConstraintsScope.EditAppModal(
    app: BankAppUi?,
    state: BankNotificationsState,
    dismiss: () -> Unit,
    onEvent: (BankNotificationsEvent) -> Unit,
) {
    var account by remember(app) { mutableStateOf(app?.account) }
    var autoAdd by remember(app) { mutableStateOf(app?.autoAdd ?: false) }

    KissosModal(
        id = remember { UUID.randomUUID() },
        visible = app != null,
        dismiss = dismiss,
        SecondaryActions = {
            ModalDelete {
                app?.let { onEvent(BankNotificationsEvent.RemoveApp(it.packageName)) }
                dismiss()
            }
        },
        PrimaryAction = {
            ModalSave {
                app?.let { onEvent(BankNotificationsEvent.SaveApp(it.packageName, account, autoAdd)) }
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = app?.label.orEmpty())
        if (app?.supported == false) {
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.bank_notifications_not_supported_description),
                style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
            )
        }
        Spacer(Modifier.height(24.dp))
        OptionsRow(
            label = stringResource(R.string.account),
            options = state.accounts,
            selected = account,
            onSelect = { account = it },
        )
        Spacer(Modifier.height(16.dp))
        SwitchRow(text = stringResource(R.string.bank_notifications_add_automatically), checked = autoAdd) {
            autoAdd = it
        }
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.bank_notifications_add_automatically_description),
            style = UI.typo.nB2.style(color = UI.colors.mediumInverse, fontWeight = FontWeight.Normal)
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun BoxWithConstraintsScope.PickAppModal(
    visible: Boolean,
    apps: List<InstalledApp>,
    dismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    KissosModal(
        id = remember { UUID.randomUUID() },
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {},
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = stringResource(R.string.bank_notifications_add_app))
        Spacer(Modifier.height(16.dp))
        apps.forEach { app ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(app.packageName) }
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = app.label, style = UI.typo.b2.style(fontWeight = FontWeight.Bold))
                if (app.supported) {
                    Text(
                        text = stringResource(R.string.bank_notifications_supported),
                        style = UI.typo.nB2.style(color = UI.colors.green, fontWeight = FontWeight.Normal)
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun openAccessSettings(context: Context) {
    try {
        context.startActivity(ListenerAccess.settingsIntent(context))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

/** Emails it to support, or copies it when there's no email app. */
private fun report(context: Context, unparsed: UnparsedUi) {
    val sent = context.emailSupport(
        subject = context.getString(R.string.bank_notifications_report_subject),
        body = context.getString(
            R.string.bank_notifications_report_body,
            unparsed.app,
            unparsed.packageName,
            unparsed.text,
            context.kissosVersion,
            androidVersion,
        ),
    )
    if (!sent) {
        context.getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText("notification", unparsed.text))
        Toast.makeText(
            context,
            context.getString(R.string.bank_notifications_report_no_email, SupportEmail),
            Toast.LENGTH_LONG,
        ).show()
    }
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview(Theme.LIGHT) {
        UI(state = previewState, onEvent = {})
    }
}

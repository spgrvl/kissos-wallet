package com.ivy.transaction

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.TransactionId
import com.ivy.design.l0_system.Orange
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.legacy.ui.theme.components.OptionUi
import com.ivy.legacy.legacy.ui.theme.components.OptionsRow
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyNameTextField
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalPositiveButton
import com.ivy.wallet.ui.theme.modal.ModalSkip
import com.ivy.wallet.ui.theme.modal.ModalTitle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import java.util.UUID

@Immutable
data class ReceiptCaption(val text: String, val isWarning: Boolean)

@Immutable
sealed interface ReceiptDialog {
    /** The QR code isn't from a Greek cash register, e.g. an e-invoicing provider's. */
    data class NotAadeReceipt(val qrText: String) : ReceiptDialog

    data class Duplicate(val transaction: TransactionId) : ReceiptDialog

    /** Asked the first time an expense from this shop is saved. */
    data class RememberMerchant(
        val merchantName: String,
        val suggestedName: String,
        val category: CategoryId?,
    ) : ReceiptDialog
}

/**
 * Opens Google's code scanner (no camera permission needed) and returns a function that starts it.
 * [onScan] gets the raw QR text; [onCancel] runs when the user backs out.
 * [onDownload] runs when Google Play services first has to download the scanner.
 */
@Composable
fun rememberReceiptScanner(
    onScan: (String) -> Unit,
    onCancel: () -> Unit,
    onUnavailable: () -> Unit,
    onDownload: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val currentOnScanned by rememberUpdatedState(onScan)
    val currentOnCancelled by rememberUpdatedState(onCancel)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val currentOnDownload by rememberUpdatedState(onDownload)
    return remember(context) {
        {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()
            val scanner = GmsBarcodeScanning.getClient(context, options)
            val startScan = {
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        barcode.rawValue?.let(currentOnScanned) ?: currentOnCancelled()
                    }
                    .addOnCanceledListener { currentOnCancelled() }
                    .addOnFailureListener { error ->
                        Timber.w(error, "Receipt scanner failed: %s", (error as? MlKitException)?.errorCode)
                        // Backing out of the scanner is sometimes reported as a failure too
                        if ((error as? MlKitException)?.errorCode == MlKitException.UNAVAILABLE) {
                            currentOnUnavailable()
                        } else {
                            currentOnCancelled()
                        }
                    }
                Unit
            }
            val modules = ModuleInstall.getClient(context)
            modules.areModulesAvailable(scanner)
                .addOnSuccessListener { response ->
                    if (response.areModulesAvailable()) {
                        startScan()
                    } else {
                        currentOnDownload()
                        installScanner(modules, scanner, onInstall = startScan, onFail = currentOnUnavailable)
                    }
                }
                .addOnFailureListener { startScan() }
        }
    }
}

/**
 * The scanner ships with Google Play services but is only preinstalled for apps from the Play Store,
 * so sideloaded builds download it on first use.
 */
private fun installScanner(
    modules: ModuleInstallClient,
    scanner: GmsBarcodeScanner,
    onInstall: () -> Unit,
    onFail: () -> Unit,
) {
    val listener = object : InstallStatusListener {
        override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
            when (update.installState) {
                ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> {
                    modules.unregisterListener(this)
                    onInstall()
                }

                ModuleInstallStatusUpdate.InstallState.STATE_FAILED,
                ModuleInstallStatusUpdate.InstallState.STATE_CANCELED -> {
                    Timber.w("Receipt scanner download failed: %s", update.errorCode)
                    modules.unregisterListener(this)
                    onFail()
                }
            }
        }
    }
    val request = ModuleInstallRequest.newBuilder()
        .addApi(scanner)
        .setListener(listener)
        .build()
    modules.installModules(request)
        .addOnSuccessListener { response ->
            if (response.areModulesAlreadyInstalled()) {
                modules.unregisterListener(listener)
                onInstall()
            }
        }
        .addOnFailureListener { error ->
            Timber.w(error, "Receipt scanner download failed")
            modules.unregisterListener(listener)
            onFail()
        }
}

/** Looking a scanned receipt up: its steps, and which one is running. */
@Immutable
data class ReceiptProgress(val steps: ImmutableList<String>, val current: Int)

/** A spinner beside the steps: the finished ones ticked, the running one in bold, the rest faded. */
@Composable
fun ReceiptProgressView(
    progress: ReceiptProgress,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(horizontal = 24.dp)) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
            color = UI.colors.primary,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            progress.steps.forEachIndexed { index, step ->
                val done = index < progress.current
                val running = index == progress.current
                Text(
                    text = if (done) "✓ $step" else step,
                    style = UI.typo.nB2.style(
                        color = when {
                            running -> UI.colors.pureInverse
                            done -> UI.colors.mediumInverse
                            else -> UI.colors.gray
                        },
                        fontWeight = if (running) FontWeight.Bold else FontWeight.Normal,
                    )
                )
            }
        }
    }
}

@Composable
fun ReceiptCaptionText(
    caption: ReceiptCaption,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(horizontal = 24.dp),
        text = caption.text,
        style = UI.typo.nB2.style(
            color = if (caption.isWarning) Orange else UI.colors.mediumInverse,
            fontWeight = FontWeight.Normal
        )
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
fun BoxWithConstraintsScope.ReceiptDialogs(
    dialog: ReceiptDialog?,
    categories: ImmutableList<Category>,
    onEvent: (EditTransactionViewEvent.ReceiptEvent) -> Unit,
    onEnterManually: () -> Unit,
) {
    val dismiss = { onEvent(EditTransactionViewEvent.ReceiptEvent.DismissDialog) }

    NotAadeReceiptModal(
        dialog = dialog as? ReceiptDialog.NotAadeReceipt,
        dismiss = dismiss,
        onEnterManually = {
            dismiss()
            onEnterManually()
        }
    )

    val duplicate = dialog as? ReceiptDialog.Duplicate
    MessageModal(
        visible = duplicate != null,
        title = stringResource(R.string.duplicate_receipt_title),
        description = stringResource(R.string.duplicate_receipt_description),
        dismiss = dismiss,
    ) {
        ModalPositiveButton(text = stringResource(R.string.open_it), iconStart = R.drawable.ic_edit) {
            duplicate?.let { onEvent(EditTransactionViewEvent.ReceiptEvent.OpenDuplicate(it.transaction)) }
        }
    }

    RememberMerchantModal(
        dialog = dialog as? ReceiptDialog.RememberMerchant,
        categories = categories,
        dismiss = dismiss,
        onAnswer = { displayName, category ->
            onEvent(EditTransactionViewEvent.ReceiptEvent.AnswerRememberMerchant(displayName, category))
        },
    )
}

@Composable
private fun BoxWithConstraintsScope.NotAadeReceiptModal(
    dialog: ReceiptDialog.NotAadeReceipt?,
    dismiss: () -> Unit,
    onEnterManually: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    MessageModal(
        visible = dialog != null,
        title = stringResource(R.string.not_aade_receipt_title),
        description = stringResource(R.string.not_aade_receipt_description),
        dismiss = dismiss,
        secondaryActions = {
            ModalSkip(text = stringResource(R.string.report_receipt)) {
                dialog?.let {
                    if (!context.emailReceiptReport(it.qrText)) {
                        clipboard.setText(AnnotatedString(it.qrText))
                        Toast.makeText(
                            context,
                            context.getString(R.string.report_receipt_no_email, ReportReceiptEmail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        },
    ) {
        ModalPositiveButton(
            text = stringResource(R.string.enter_manually),
            iconStart = R.drawable.ic_edit,
            onClick = onEnterManually,
        )
    }
}

/** Where unsupported receipts are reported, so their providers can be added. */
private const val ReportReceiptEmail = "contact.sgsoftware@gmail.com"

/** Opens an email to [ReportReceiptEmail] with the QR text filled in; false when there's no email app. */
private fun Context.emailReceiptReport(qrText: String): Boolean {
    val version = packageManager.getPackageInfo(packageName, 0).versionName
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(ReportReceiptEmail))
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_receipt_subject))
        putExtra(Intent.EXTRA_TEXT, getString(R.string.report_receipt_body, qrText, version, Build.VERSION.RELEASE))
    }
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No email app to report a receipt")
        false
    }
}

@Composable
private fun BoxWithConstraintsScope.RememberMerchantModal(
    dialog: ReceiptDialog.RememberMerchant?,
    categories: ImmutableList<Category>,
    dismiss: () -> Unit,
    onAnswer: (displayName: String?, category: CategoryId?) -> Unit,
) {
    var name by remember(dialog) { mutableStateOf(TextFieldValue(dialog?.suggestedName.orEmpty())) }
    var category by remember(dialog) { mutableStateOf(dialog?.category?.value) }
    val options = remember(categories) {
        categories.map { OptionUi(it.id.value, it.name.value, it.color.value) }.toImmutableList()
    }
    IvyModal(
        id = remember { UUID.randomUUID() },
        visible = dialog != null,
        dismiss = dismiss,
        SecondaryActions = {
            ModalSkip(text = stringResource(R.string.not_now)) { onAnswer(null, null) }
        },
        PrimaryAction = {
            ModalPositiveButton(
                text = stringResource(R.string.remember),
                iconStart = R.drawable.ic_check,
                enabled = name.text.isNotBlank(),
            ) {
                onAnswer(name.text, category?.let(::CategoryId))
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = stringResource(R.string.remember_merchant_title))
        Spacer(Modifier.height(16.dp))
        ModalDescription(stringResource(R.string.remember_merchant_description, dialog?.merchantName.orEmpty()))
        Spacer(Modifier.height(24.dp))
        IvyNameTextField(
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
            options = options,
            selected = category,
            onSelect = { category = it },
        )
        Spacer(Modifier.height(48.dp))
    }
}

@Suppress("ComposableParametersOrdering")
@Composable
private fun BoxWithConstraintsScope.MessageModal(
    visible: Boolean,
    title: String,
    description: String,
    dismiss: () -> Unit,
    secondaryActions: (@Composable () -> Unit)? = null,
    primaryAction: @Composable () -> Unit,
) {
    IvyModal(
        id = remember { UUID.randomUUID() },
        visible = visible,
        dismiss = dismiss,
        SecondaryActions = secondaryActions,
        PrimaryAction = primaryAction,
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = title)
        Spacer(Modifier.height(16.dp))
        ModalDescription(description)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ModalDescription(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = text,
        style = UI.typo.b2.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.Medium
        )
    )
}

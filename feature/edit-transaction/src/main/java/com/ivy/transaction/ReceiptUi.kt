package com.ivy.transaction

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ivy.data.model.TransactionId
import com.ivy.design.l0_system.Orange
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyNameTextField
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalPositiveButton
import com.ivy.wallet.ui.theme.modal.ModalSkip
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Immutable
data class ReceiptCaption(val text: String, val isWarning: Boolean)

@Immutable
sealed interface ReceiptDialog {
    /** The QR code isn't from a Greek cash register, e.g. an e-invoicing provider's. */
    data class NotAadeReceipt(val qrText: String) : ReceiptDialog

    data class Duplicate(val transaction: TransactionId) : ReceiptDialog

    /** Asked the first time an expense from this shop is saved. */
    data class RememberMerchant(val merchantName: String, val suggestedName: String) : ReceiptDialog
}

/**
 * Opens Google's code scanner (no camera permission needed) and returns a function that starts it.
 * [onScan] gets the raw QR text; [onCancel] runs when the user backs out.
 */
@Composable
fun rememberReceiptScanner(
    onScan: (String) -> Unit,
    onCancel: () -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val currentOnScanned by rememberUpdatedState(onScan)
    val currentOnCancelled by rememberUpdatedState(onCancel)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    return remember(context) {
        {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()
            GmsBarcodeScanning.getClient(context, options)
                .startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let(currentOnScanned) ?: currentOnCancelled()
                }
                .addOnCanceledListener { currentOnCancelled() }
                .addOnFailureListener { currentOnUnavailable() }
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
        dismiss = dismiss,
        onAnswer = { onEvent(EditTransactionViewEvent.ReceiptEvent.AnswerRememberMerchant(it)) },
    )
}

@Composable
private fun BoxWithConstraintsScope.NotAadeReceiptModal(
    dialog: ReceiptDialog.NotAadeReceipt?,
    dismiss: () -> Unit,
    onEnterManually: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    MessageModal(
        visible = dialog != null,
        title = stringResource(R.string.not_aade_receipt_title),
        description = stringResource(R.string.not_aade_receipt_description),
        dismiss = dismiss,
        secondaryActions = {
            ModalSkip(text = stringResource(R.string.copy_qr_text)) {
                dialog?.let { clipboard.setText(AnnotatedString(it.qrText)) }
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

@Composable
private fun BoxWithConstraintsScope.RememberMerchantModal(
    dialog: ReceiptDialog.RememberMerchant?,
    dismiss: () -> Unit,
    onAnswer: (displayName: String?) -> Unit,
) {
    var name by remember(dialog) { mutableStateOf(TextFieldValue(dialog?.suggestedName.orEmpty())) }
    IvyModal(
        id = remember { UUID.randomUUID() },
        visible = dialog != null,
        dismiss = dismiss,
        SecondaryActions = {
            ModalSkip(text = stringResource(R.string.not_now)) { onAnswer(null) }
        },
        PrimaryAction = {
            ModalPositiveButton(
                text = stringResource(R.string.remember),
                iconStart = R.drawable.ic_check,
                enabled = name.text.isNotBlank(),
            ) {
                onAnswer(name.text)
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

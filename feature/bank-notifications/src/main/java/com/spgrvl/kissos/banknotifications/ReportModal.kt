package com.spgrvl.kissos.banknotifications

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.SupportEmail
import com.spgrvl.kissos.ui.emailSupport
import com.spgrvl.kissos.wallet.ui.theme.modal.KissosModal
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalPositiveButton
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

/** Says what a report sends, and to whom, before the email app opens. */
@Composable
internal fun BoxWithConstraintsScope.ReportModal(
    visible: Boolean,
    title: String,
    text: String,
    dismiss: () -> Unit,
    onSend: () -> Unit,
) {
    KissosModal(
        id = remember { UUID.randomUUID() },
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalPositiveButton(
                text = stringResource(R.string.open_email),
                iconStart = R.drawable.ic_check,
                onClick = onSend,
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))
        ModalTitle(text = title)
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = text,
            style = UI.typo.nB2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Normal)
        )
        Spacer(Modifier.height(32.dp))
    }
}

/** Emails a report to support, or copies [copied] when there's no email app. */
internal fun Context.sendReport(subject: String, body: String, copied: String, @StringRes noEmail: Int) {
    if (emailSupport(subject = subject, body = body)) return
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("report", copied))
    Toast.makeText(this, getString(noEmail, SupportEmail), Toast.LENGTH_LONG).show()
}

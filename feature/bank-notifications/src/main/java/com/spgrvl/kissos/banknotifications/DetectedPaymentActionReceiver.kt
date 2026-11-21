package com.spgrvl.kissos.banknotifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spgrvl.kissos.domain.banknotification.DetectedPayments
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** The Add, Dismiss and Undo buttons of Kissos's notifications about detected payments. */
@AndroidEntryPoint
class DetectedPaymentActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var payments: DetectedPayments

    @Inject
    lateinit var notifier: DetectedPaymentNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.data?.lastPathSegment
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(intent.action, id)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(action: String?, id: UUID) {
        when (action) {
            DetectedPaymentNotifier.ACTION_ADD -> {
                notifier.cancel(id)
                if (payments.add(id)) {
                    payments.find(id)?.let { notifier.showAdded(payments.draft(it)) }
                }
            }

            DetectedPaymentNotifier.ACTION_DISMISS -> {
                payments.dismiss(id)
                notifier.cancel(id)
            }

            DetectedPaymentNotifier.ACTION_UNDO -> {
                payments.undo(id)
                payments.find(id)?.let { notifier.showDetected(payments.draft(it)) }
            }
        }
    }
}

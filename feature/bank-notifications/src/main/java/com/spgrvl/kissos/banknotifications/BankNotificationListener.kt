package com.spgrvl.kissos.banknotifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spgrvl.kissos.banknotifications.parse.NotificationText
import com.spgrvl.kissos.domain.banknotification.BankNotificationPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Receives the notifications of the bank apps the user picked. The system hands every app's notifications
 * to a listener, so each one is checked against the user's apps first, and the others are dropped
 * without reading anything in them.
 */
@AndroidEntryPoint
class BankNotificationListener : NotificationListenerService() {
    @Inject
    lateinit var prefs: BankNotificationPrefs

    @Inject
    lateinit var processor: BankNotificationProcessor

    private var scope: CoroutineScope? = null

    /** The user's apps, each with when its notifications are read from. */
    @Volatile
    private var allowedPackages: Map<String, Long> = emptyMap()

    override fun onListenerConnected() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default).also { scope = it }
        // Turned off, now or while it wasn't running (e.g. all data was deleted): stops receiving notifications
        // until it's turned on again, even after a restart
        scope.launch {
            prefs.enabled.first { !it }
            allowedPackages = emptyMap()
            requestUnbind()
        }
        scope.launch {
            if (!prefs.enabled.first()) return@launch
            allowedPackages = prefs.allowedPackages.first()
            processor.purge()
            // Payments notified while Kissos wasn't listening; the ones already seen are skipped
            activeNotifications.orEmpty().forEach(::onNotificationPosted)
            prefs.allowedPackages.collect { allowedPackages = it }
        }
    }

    override fun onListenerDisconnected() {
        scope?.cancel()
        scope = null
    }

    override fun onDestroy() {
        onListenerDisconnected()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Before the user picked the app or turned this on, e.g. still shown since then: not read
        val readFrom = allowedPackages[sbn.packageName]
        val notification = sbn.notification
        // Android groups an app's notifications under a summary of its own; each payment is still separate
        val summaryOrOngoing =
            notification.flags and (Notification.FLAG_GROUP_SUMMARY or Notification.FLAG_ONGOING_EVENT) != 0
        if (readFrom == null || sbn.postTime < readFrom || summaryOrOngoing) return
        val extras = notification.extras
        val text = NotificationText(
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
        )
        val postedAt = Instant.ofEpochMilli(notification.`when`.takeIf { it > 0 } ?: sbn.postTime)
        scope?.launch {
            processor.process(
                packageName = sbn.packageName,
                notificationKey = sbn.key,
                postedAt = postedAt,
                text = text,
            )
        }
    }
}

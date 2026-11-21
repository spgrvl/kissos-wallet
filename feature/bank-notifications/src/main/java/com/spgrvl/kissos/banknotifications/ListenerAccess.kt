package com.spgrvl.kissos.banknotifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat

/** Turns the notification listener on and off, and checks and asks for the system's notification access. */
object ListenerAccess {
    private fun component(context: Context): ComponentName =
        ComponentName(context, BankNotificationListener::class.java)

    /** Whether the user granted Kissos notification access in the system settings. */
    fun isGranted(context: Context): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)

    /**
     * Enables the listener, which is disabled until the user first turns bank notifications on, so that until
     * then Kissos isn't even listed in the system's notification access settings. Turning them off later is done
     * by the listener itself, which unbinds from the system (see BankNotificationListener).
     */
    fun startListening(context: Context) {
        context.packageManager.setComponentEnabledSetting(
            component(context),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
        rebind(context)
    }

    /** Asks the system to run the listener again after it unbound itself, if the user allows it. */
    fun rebind(context: Context) {
        if (isGranted(context)) NotificationListenerService.requestRebind(component(context))
    }

    /** The system page to grant or revoke Kissos's notification access. */
    fun settingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component(context).flattenToString())
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

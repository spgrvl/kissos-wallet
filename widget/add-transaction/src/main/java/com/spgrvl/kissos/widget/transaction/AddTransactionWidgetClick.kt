package com.spgrvl.kissos.widget.transaction

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.domain.AppStarter
import javax.inject.Inject

class AddTransactionWidgetClick @Inject constructor(
    private val appStarter: AppStarter
) {
    // Opens RootActivity directly: since Android 15, a broadcast that then
    // starts an activity (a trampoline) may be blocked as a background launch.
    fun clickListener(
        context: Context,
        rv: RemoteViews,
        @IdRes viewId: Int,
        type: TransactionType,
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            type.ordinal,
            appStarter.addTransactionStartIntent(type),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        rv.setOnClickPendingIntent(viewId, pendingIntent)
    }

    fun scanReceiptClickListener(
        context: Context,
        rv: RemoteViews,
        @IdRes viewId: Int,
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            SCAN_RECEIPT_REQUEST_CODE,
            appStarter.scanReceiptStartIntent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        rv.setOnClickPendingIntent(viewId, pendingIntent)
    }

    private companion object {
        // Apart from the transaction types' ordinals
        const val SCAN_RECEIPT_REQUEST_CODE = 100
    }
}

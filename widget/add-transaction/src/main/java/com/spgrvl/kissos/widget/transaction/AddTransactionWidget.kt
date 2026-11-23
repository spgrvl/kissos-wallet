package com.spgrvl.kissos.widget.transaction

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.ui.AppLanguage
import com.spgrvl.kissos.ui.R as UiR
import com.spgrvl.kissos.widgets.WidgetBase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddTransactionWidget : AppWidgetProvider() {

    @Inject
    lateinit var widgetClick: AddTransactionWidgetClick

    companion object {
        fun updateBroadcast(context: Context) {
            WidgetBase.updateBroadcast(context, AddTransactionWidget::class.java)
        }

        private const val LABEL_SP = 12f
        private const val MIN_LABEL_SP = 9f
        private const val SLOTS = 4
        private const val SIDE_PADDING_DP = 32 // the layout's 16dp on each side
        private const val SLOT_FILL = 0.92f // leaves a little space between labels
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateBroadcast(context)
    }

    // --------------------------- </BROADCASTS> ----------------------------------------------------
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val rv = RemoteViews(context.packageName, R.layout.widget_add_transaction)

        setLabels(context, rv, appWidgetManager.getAppWidgetOptions(appWidgetId))

        widgetClick.clickListener(context, rv, R.id.ivIncome, TransactionType.INCOME)
        widgetClick.clickListener(context, rv, R.id.tvIncome, TransactionType.INCOME)

        widgetClick.clickListener(context, rv, R.id.ivExpense, TransactionType.EXPENSE)
        widgetClick.clickListener(context, rv, R.id.tvExpense, TransactionType.EXPENSE)

        widgetClick.clickListener(context, rv, R.id.ivTransfer, TransactionType.TRANSFER)
        widgetClick.clickListener(context, rv, R.id.tvTransfer, TransactionType.TRANSFER)

        widgetClick.scanReceiptClickListener(context, rv, R.id.ivScanReceipt)
        widgetClick.scanReceiptClickListener(context, rv, R.id.tvScanReceipt)

        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }

    // Resized: the labels' size depends on the width
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    /**
     * The labels in the app's language (the launcher would draw the layout's strings in the
     * phone's), all at one size that fits the narrowest slot; hidden when that is too small to
     * read, leaving the icons as on the compact widget.
     */
    private fun setLabels(context: Context, rv: RemoteViews, options: Bundle?) {
        val strings = AppLanguage.localized(context).resources
        val labels = listOf(
            R.id.tvIncome to strings.getString(UiR.string.income),
            R.id.tvExpense to strings.getString(UiR.string.expense),
            R.id.tvTransfer to strings.getString(UiR.string.transfer),
            R.id.tvScanReceipt to strings.getString(UiR.string.widget_scan),
        )
        val sp = labelSize(context, labels.map { it.second }, options)
        labels.forEach { (id, text) ->
            rv.setTextViewText(id, text)
            if (sp != null) rv.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, sp)
            rv.setViewVisibility(id, if (sp != null) View.VISIBLE else View.GONE)
        }
    }

    /** The labels' size in sp, or null when they don't fit at a readable size. */
    private fun labelSize(context: Context, labels: List<String>, options: Bundle?): Float? {
        // The width in portrait, in dp; unknown (0) before the launcher has reported it
        val widthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        if (widthDp <= 0) return LABEL_SP
        val metrics = context.resources.displayMetrics
        val slotPx = (widthDp - SIDE_PADDING_DP) * metrics.density / SLOTS * SLOT_FILL
        val paint = TextPaint().apply {
            typeface = ResourcesCompat.getFont(context, UiR.font.raleway_bold)
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, LABEL_SP, metrics)
        }
        val widest = labels.maxOf(paint::measureText)
        val sp = if (widest <= slotPx) LABEL_SP else LABEL_SP * slotPx / widest
        return sp.takeIf { it >= MIN_LABEL_SP }
    }
}

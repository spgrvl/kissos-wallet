package com.spgrvl.kissos.legacy.data.model

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.legacy.pluralsRes
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.data.model.IntervalType
import com.spgrvl.kissos.legacy.incrementDate
import com.spgrvl.kissos.ui.R
import java.time.Instant

@Suppress("DataClassFunctions")
@Immutable
data class LastNTimeRange(
    val periodN: Int,
    val periodType: IntervalType,
) {
    fun fromDate(
        timeProvider: TimeProvider
    ): Instant = periodType.incrementDate(
        date = timeProvider.utcNow(),
        intervalN = -periodN.toLong()
    )

    /** E.g. "Last 3 months", or with [inSentence] "the last 3 months". */
    fun forDisplay(inSentence: Boolean = false): String {
        val plurals = when (periodType) {
            IntervalType.DAY -> if (inSentence) R.plurals.the_last_n_days else R.plurals.last_n_days
            IntervalType.WEEK -> if (inSentence) R.plurals.the_last_n_weeks else R.plurals.last_n_weeks
            IntervalType.MONTH -> if (inSentence) R.plurals.the_last_n_months else R.plurals.last_n_months
            IntervalType.YEAR -> if (inSentence) R.plurals.the_last_n_years else R.plurals.last_n_years
        }
        return pluralsRes(plurals, periodN, periodN)
    }
}

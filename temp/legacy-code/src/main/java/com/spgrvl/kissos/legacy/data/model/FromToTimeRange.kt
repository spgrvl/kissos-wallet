package com.spgrvl.kissos.legacy.data.model

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.base.legacy.Transaction
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.legacy.utils.dateNowUTC
import com.spgrvl.kissos.legacy.utils.kissosMaxTime
import com.spgrvl.kissos.legacy.utils.kissosMinTime
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.time.TimeFormatter
import com.spgrvl.kissos.wallet.domain.pure.data.ClosedTimeRange
import java.time.Instant
import java.time.ZoneOffset

@Suppress("DataClassFunctions")
@Immutable
data class FromToTimeRange(
    val from: Instant?,
    val to: Instant?,
) {
    fun from(): Instant =
        from ?: kissosMinTime()

    fun to(): Instant =
        to ?: kissosMaxTime()

    fun upcomingFrom(
        timeProvider: TimeProvider
    ): Instant {
        val startOfDayNowUTC = timeProvider.utcNow()
        return if (includes(startOfDayNowUTC)) startOfDayNowUTC else from()
    }

    fun overdueTo(
        timeProvider: TimeProvider
    ): Instant {
        val startOfDayNowUTC = timeProvider.utcNow()
        return if (includes(startOfDayNowUTC)) startOfDayNowUTC else to()
    }

    fun includes(dateTime: Instant): Boolean =
        dateTime.isAfter(from()) && dateTime.isBefore(to())

    fun toDisplay(
        timeFormatter: TimeFormatter
    ): String = with(timeFormatter) {
        val style = TimeFormatter.Style.DateOnly(includeWeekDay = false)
        when {
            from != null && to != null -> {
                "${from.formatLocal(style)} - ${to.formatLocal(style)}"
            }

            from != null && to == null -> {
                stringRes(R.string.from_date, from.formatLocal(style))
            }

            from == null && to != null -> {
                stringRes(R.string.to_date, to.formatLocal(style))
            }

            else -> {
                stringRes(R.string.range)
            }
        }
    }
}

@Deprecated("Uses legacy Transaction")
fun Iterable<Transaction>.filterUpcomingLegacy(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): List<Transaction> {
    val todayStartOfDayUtc = todayStartOfDayUtc(timeProvider, timeConverter)
    return filter {
        // make sure that it's in the future
        it.dueDate != null && it.dueDate!!.isAfter(todayStartOfDayUtc)
    }
}

fun Iterable<com.spgrvl.kissos.data.model.Transaction>.filterUpcoming():
    List<com.spgrvl.kissos.data.model.Transaction> {
    val todayStartOfDayUTC = dateNowUTC().atStartOfDay().toInstant(ZoneOffset.UTC)

    return filter {
        // make sure that it's in the future
        !it.settled && it.time.isAfter(todayStartOfDayUTC)
    }
}

@Deprecated("Uses legacy Transaction")
fun Iterable<Transaction>.filterOverdueLegacy(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): List<Transaction> {
    val todayStartOfDayUTC = todayStartOfDayUtc(timeProvider, timeConverter)
    return filter {
        // make sure that it's in the past
        it.dueDate != null && it.dueDate!!.isBefore(todayStartOfDayUTC)
    }
}

fun todayStartOfDayUtc(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): Instant = with(timeConverter) {
    timeProvider.localNow()
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .toUTC()
}

fun Iterable<com.spgrvl.kissos.data.model.Transaction>.filterOverdue(): List<com.spgrvl.kissos.data.model.Transaction> {
    val todayStartOfDayUTC = dateNowUTC().atStartOfDay().toInstant(ZoneOffset.UTC)

    return filter {
        // make sure that it's in the past
        !it.settled && it.time.isBefore(todayStartOfDayUTC)
    }
}

fun FromToTimeRange.toCloseTimeRangeUnsafe(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from(),
        to = to()
    )
}

fun FromToTimeRange.toCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: kissosMinTime(),
        to = to ?: kissosMaxTime()
    )
}

fun FromToTimeRange.toUTCCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: kissosMinTime(),
        to = to ?: kissosMaxTime()
    )
}

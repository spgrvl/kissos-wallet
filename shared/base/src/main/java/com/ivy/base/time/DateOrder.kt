package com.ivy.base.time

import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

/** Whether dates in this language put the day before the month, e.g. Greek "23 Οκτ" but US English "Oct 23". */
fun Locale.writesDayBeforeMonth(): Boolean {
    val pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.MEDIUM,
        null,
        IsoChronology.INSTANCE,
        this,
    )
    val day = pattern.indexOf('d')
    val month = pattern.indexOf('M')
    return day >= 0 && month >= 0 && day < month
}

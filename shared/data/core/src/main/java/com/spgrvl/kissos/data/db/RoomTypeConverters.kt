package com.spgrvl.kissos.domain.db

import androidx.room.TypeConverter
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.legacy.epochMilliToDateTime
import com.spgrvl.kissos.base.legacy.toEpochMilli
import com.spgrvl.kissos.base.model.LoanRecordType
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.data.model.IntervalType
import com.spgrvl.kissos.data.model.LoanType
import com.spgrvl.kissos.data.model.ReceiptStatus
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@SuppressWarnings("unused")
class RoomTypeConverters {
    @TypeConverter
    fun saveDate(localDateTime: LocalDateTime?): Long? = localDateTime?.toEpochMilli()

    @TypeConverter
    fun parseDate(timestampMillis: Long?): LocalDateTime? = timestampMillis?.epochMilliToDateTime()

    @TypeConverter
    fun saveUUID(id: UUID?) = id?.toString()

    @TypeConverter
    fun parseUUID(id: String?) = id?.let { UUID.fromString(id) }

    @TypeConverter
    fun saveTheme(value: Theme?) = value?.name

    @TypeConverter
    fun parseTheme(value: String?) = value?.let { Theme.valueOf(it) }

    @TypeConverter
    fun saveTransactionType(value: TransactionType?) = value?.name

    @TypeConverter
    fun parseTransactionType(value: String?) = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun saveRecurringIntervalType(value: IntervalType?) = value?.name

    @TypeConverter
    fun parseRecurringIntervalType(value: String?) =
        value?.let { IntervalType.valueOf(it) }

    @TypeConverter
    fun saveLoanType(value: LoanType?) = value?.name

    @TypeConverter
    fun parseLoanType(value: String?) = value?.let { LoanType.valueOf(it) }

    @TypeConverter
    fun saveInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun parseInstant(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun saveLoanRecordType(value: LoanRecordType?): String? = value?.name

    @TypeConverter
    fun parseLoanRecordType(value: String?): LoanRecordType? =
        value?.let { LoanRecordType.valueOf(it) }

    @TypeConverter
    fun saveReceiptStatus(value: ReceiptStatus?): String? = value?.name

    @TypeConverter
    fun parseReceiptStatus(value: String?): ReceiptStatus? =
        value?.let { ReceiptStatus.valueOf(it) }
}

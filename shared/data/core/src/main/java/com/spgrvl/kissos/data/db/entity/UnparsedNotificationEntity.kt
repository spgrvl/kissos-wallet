package com.spgrvl.kissos.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

/**
 * A notification from a bank app the user picked that no parser understood. Only kept if the user opts in,
 * for a week, so they can report a new format. Not in backups.
 */
@Keep
@Entity(tableName = "unparsed_notifications")
data class UnparsedNotificationEntity(
    @PrimaryKey
    val id: UUID,
    val sourcePackage: String,
    val title: String?,
    val text: String,
    val postedAt: Instant,
)

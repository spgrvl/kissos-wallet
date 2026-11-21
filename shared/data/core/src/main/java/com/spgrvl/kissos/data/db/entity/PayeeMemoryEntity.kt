package com.spgrvl.kissos.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spgrvl.kissos.base.kotlinxserilzation.KSerializerInstant
import com.spgrvl.kissos.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/** What the user calls a payee from bank notifications, e.g. "POWERCO" → "Electricity", and its category. */
@Keep
@Serializable
@Entity(tableName = "payee_memory")
data class PayeeMemoryEntity(
    /** The payee as banks write it, lowercased and with spaces collapsed. */
    @PrimaryKey
    @SerialName("payee")
    val payee: String,
    @SerialName("title")
    val title: String,
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID?,
    @SerialName("updatedAt")
    @Serializable(with = KSerializerInstant::class)
    val updatedAt: Instant,
)

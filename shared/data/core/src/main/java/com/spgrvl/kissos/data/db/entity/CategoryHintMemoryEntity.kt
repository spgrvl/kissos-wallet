package com.spgrvl.kissos.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import com.spgrvl.kissos.base.kotlinxserilzation.KSerializerInstant
import com.spgrvl.kissos.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/** The user's category for a bank's own one, e.g. Revolut's "❤️" → Health, learned from their choices. */
@Keep
@Serializable
@Entity(tableName = "category_hint_memory", primaryKeys = ["sourcePackage", "hint"])
data class CategoryHintMemoryEntity(
    @SerialName("sourcePackage")
    val sourcePackage: String,
    @SerialName("hint")
    val hint: String,
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID,
    @SerialName("updatedAt")
    @Serializable(with = KSerializerInstant::class)
    val updatedAt: Instant,
)

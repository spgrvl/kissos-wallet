package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/** Merchant memory: what to fill in for receipts from the shop with this tax id (ΑΦΜ). */
@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey
    @SerialName("afm")
    val afm: String,
    @SerialName("displayName")
    val displayName: String,
    @SerialName("defaultCategoryId")
    @Serializable(with = KSerializerUUID::class)
    val defaultCategoryId: UUID? = null,
    @SerialName("defaultAccountId")
    @Serializable(with = KSerializerUUID::class)
    val defaultAccountId: UUID? = null,
    @SerialName("updatedAt")
    @Serializable(with = KSerializerInstant::class)
    val updatedAt: Instant,
)

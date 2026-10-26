package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import com.ivy.data.model.ReceiptStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * A scanned AADE cash-register receipt. Deleted together with its transaction.
 * VAT columns are per VAT category: A = Α, B = Β, C = Γ, D = Δ, E = Ε.
 */
@Suppress("DataClassDefaultValues", "DataClassTypedIDs")
@Keep
@Serializable
@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["registerId", "receiptNumber"], unique = true)],
)
data class ReceiptEntity(
    @PrimaryKey
    @SerialName("transactionId")
    @Serializable(with = KSerializerUUID::class)
    val transactionId: UUID,
    @SerialName("sig")
    val sig: String,
    @SerialName("registerId")
    val registerId: String,
    @SerialName("receiptNumber")
    val receiptNumber: Long,
    @SerialName("total")
    val total: Double,
    @SerialName("issuedAt")
    @Serializable(with = KSerializerInstant::class)
    val issuedAt: Instant? = null,
    @SerialName("merchantAfm")
    val merchantAfm: String? = null,
    @SerialName("merchantName")
    val merchantName: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("documentType")
    val documentType: String? = null,
    @SerialName("cardAmount")
    val cardAmount: Double? = null,
    @SerialName("netA")
    val netA: Double? = null,
    @SerialName("vatA")
    val vatA: Double? = null,
    @SerialName("netB")
    val netB: Double? = null,
    @SerialName("vatB")
    val vatB: Double? = null,
    @SerialName("netC")
    val netC: Double? = null,
    @SerialName("vatC")
    val vatC: Double? = null,
    @SerialName("netD")
    val netD: Double? = null,
    @SerialName("vatD")
    val vatD: Double? = null,
    @SerialName("netE")
    val netE: Double? = null,
    @SerialName("vatE")
    val vatE: Double? = null,
    @SerialName("status")
    val status: ReceiptStatus,
    @SerialName("aadeMessage")
    val aadeMessage: String? = null,
    @SerialName("scannedAt")
    @Serializable(with = KSerializerInstant::class)
    val scannedAt: Instant,
    @SerialName("lastCheckedAt")
    @Serializable(with = KSerializerInstant::class)
    val lastCheckedAt: Instant? = null,
)

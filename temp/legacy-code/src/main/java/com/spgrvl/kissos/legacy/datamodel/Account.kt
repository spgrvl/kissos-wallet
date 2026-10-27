package com.spgrvl.kissos.legacy.datamodel

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.raise.either
import com.spgrvl.kissos.data.db.entity.AccountEntity
import com.spgrvl.kissos.data.model.Account
import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.primitive.AssetCode
import com.spgrvl.kissos.data.model.primitive.ColorInt
import com.spgrvl.kissos.data.model.primitive.IconAsset
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.data.repository.CurrencyRepository
import java.util.UUID
import com.spgrvl.kissos.data.model.Account as DomainAccount

@Deprecated("Legacy data model. Will be deleted")
@Immutable
data class Account(
    val name: String,
    val color: Int,
    val currency: String? = null,
    val icon: String? = null,
    val orderNum: Double = 0.0,
    val includeInBalance: Boolean = true,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    fun toEntity(): AccountEntity = AccountEntity(
        name = name,
        currency = currency,
        color = color,
        icon = icon,
        orderNum = orderNum,
        includeInBalance = includeInBalance,
        isSynced = isSynced,
        isDeleted = isDeleted,
        id = id
    )

    @Suppress("DataClassFunctions")
    suspend fun toDomainAccount(
        currencyRepository: CurrencyRepository
    ): Either<String, DomainAccount> {
        return either {
            Account(
                id = AccountId(id),
                name = NotBlankTrimmedString.from(name).bind(),
                asset = currency?.let(AssetCode::from)?.bind()
                    ?: currencyRepository.getBaseCurrency(),
                color = ColorInt(color),
                icon = icon?.let(IconAsset::from)?.getOrNull(),
                includeInBalance = includeInBalance,
                orderNum = orderNum,
            )
        }
    }
}

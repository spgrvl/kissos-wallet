package com.spgrvl.kissos.data.repository.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.spgrvl.kissos.data.db.entity.CategoryEntity
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.data.model.primitive.ColorInt
import com.spgrvl.kissos.data.model.primitive.IconAsset
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import javax.inject.Inject

class CategoryMapper @Inject constructor() {
    fun CategoryEntity.toDomain(): Either<String, Category> = either {
        ensure(!isDeleted) { "Category is deleted" }

        Category(
            id = com.spgrvl.kissos.data.model.CategoryId(id),
            name = NotBlankTrimmedString.from(name).bind(),
            color = ColorInt(color),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            orderNum = orderNum,
        )
    }

    fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            name = name.value,
            color = color.value,
            icon = icon?.id,
            orderNum = orderNum,
            isSynced = true,
            id = id.value
        )
    }
}

package com.spgrvl.kissos.wallet.domain.deprecated.logic

import androidx.compose.ui.graphics.toArgb
import arrow.core.raise.either
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.model.primitive.ColorInt
import com.spgrvl.kissos.data.model.primitive.IconAsset
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.CategoryRepository
import com.spgrvl.kissos.data.repository.CurrencyRepository
import com.spgrvl.kissos.design.l0_system.Blue
import com.spgrvl.kissos.design.l0_system.Blue2
import com.spgrvl.kissos.design.l0_system.Blue2Light
import com.spgrvl.kissos.design.l0_system.Blue3
import com.spgrvl.kissos.design.l0_system.BlueDark
import com.spgrvl.kissos.design.l0_system.BlueLight
import com.spgrvl.kissos.design.l0_system.Green
import com.spgrvl.kissos.design.l0_system.Green2
import com.spgrvl.kissos.design.l0_system.Green2Light
import com.spgrvl.kissos.design.l0_system.GreenLight
import com.spgrvl.kissos.design.l0_system.Ivy
import com.spgrvl.kissos.design.l0_system.IvyDark
import com.spgrvl.kissos.design.l0_system.IvyLight
import com.spgrvl.kissos.design.l0_system.Orange
import com.spgrvl.kissos.design.l0_system.Orange2
import com.spgrvl.kissos.design.l0_system.Orange3
import com.spgrvl.kissos.design.l0_system.Orange3Light
import com.spgrvl.kissos.design.l0_system.Purple1
import com.spgrvl.kissos.design.l0_system.Purple2
import com.spgrvl.kissos.design.l0_system.Red
import com.spgrvl.kissos.design.l0_system.Red3
import com.spgrvl.kissos.design.l0_system.Red3Light
import com.spgrvl.kissos.design.l0_system.RedLight
import com.spgrvl.kissos.design.l0_system.Yellow
import com.spgrvl.kissos.design.l0_system.YellowLight
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateCategoryData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID
import javax.inject.Inject

@Deprecated("Legacy, get rid of it.")
class PreloadDataLogic @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val currencyRepository: CurrencyRepository,
) {
    private var categoryOrderNum = 0.0

    suspend fun preloadAccounts() {
        val cash = Account(
            name = stringRes(R.string.cash),
            currency = null,
            color = Green.toArgb(),
            icon = "cash",
            orderNum = 0.0,
            isSynced = false
        )

        val bank = Account(
            name = stringRes(R.string.bank),
            currency = null,
            color = IvyDark.toArgb(),
            icon = "bank",
            orderNum = 1.0,
            isSynced = false
        )

        cash.toDomainAccount(currencyRepository).getOrNull()
            ?.let { accountRepository.save(it) }
        bank.toDomainAccount(currencyRepository).getOrNull()
            ?.let { accountRepository.save(it) }
    }

    fun accountSuggestions(baseCurrency: String): ImmutableList<CreateAccountData> =
        persistentListOf(
            CreateAccountData(
                name = stringRes(R.string.cash),
                currency = baseCurrency,
                color = Green,
                icon = "cash",
                balance = 0.0
            ),
            CreateAccountData(
                name = stringRes(R.string.bank),
                currency = baseCurrency,
                color = IvyDark,
                icon = "bank",
                balance = 0.0
            ),
            CreateAccountData(
                name = stringRes(R.string.revoult),
                currency = baseCurrency,
                color = Blue,
                icon = "revolut",
                balance = 0.0
            ),
        )

    suspend fun preloadCategories() {
        categoryOrderNum = 0.0

        val categoriesToPreload = preloadCategoriesCreateData()

        for (createData in categoriesToPreload) {
            preloadCategory(createData)
        }
    }

    private fun preloadCategoriesCreateData() = listOf(
        CreateCategoryData(
            name = stringRes(R.string.food_drinks),
            color = Green,
            icon = "fooddrink"
        ),

        CreateCategoryData(
            name = stringRes(R.string.bills_fees),
            color = Red,
            icon = "bills"
        ),

        CreateCategoryData(
            name = stringRes(R.string.transport),
            color = YellowLight,
            icon = "transport"
        ),

        CreateCategoryData(
            name = stringRes(R.string.groceries),
            color = GreenLight,
            icon = "groceries"
        ),

        CreateCategoryData(
            name = stringRes(R.string.entertainment),
            color = Orange,
            icon = "game"
        ),

        CreateCategoryData(
            name = stringRes(R.string.shopping),
            color = Ivy,
            icon = "shopping"
        ),

        CreateCategoryData(
            name = stringRes(R.string.gifts),
            color = RedLight,
            icon = "gift"
        ),

        CreateCategoryData(
            name = stringRes(R.string.health),
            color = IvyLight,
            icon = "health"
        ),

        CreateCategoryData(
            name = stringRes(R.string.investments),
            color = IvyDark,
            icon = "leaf"
        ),

        CreateCategoryData(
            name = stringRes(R.string.loans),
            color = BlueDark,
            icon = "loan"
        ),
    )

    private suspend fun preloadCategory(
        data: CreateCategoryData,
    ) {
        val category: Category? = either {
            Category(
                name = NotBlankTrimmedString.from(data.name).bind(),
                color = ColorInt(data.color.toArgb()),
                icon = data.icon?.let(IconAsset::from)?.getOrNull(),
                orderNum = categoryOrderNum++,
                id = CategoryId(UUID.randomUUID()),
            )
        }.getOrNull()

        if (category != null) {
            categoryRepository.save(category)
        }
    }

    fun categorySuggestions(): ImmutableList<CreateCategoryData> = preloadCategoriesCreateData()
        .plus(
            listOf(
                CreateCategoryData(
                    name = stringRes(R.string.car),
                    color = Blue3,
                    icon = "vehicle"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.work),
                    color = Blue2Light,
                    icon = "work"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.home_category),
                    color = Green2,
                    icon = "house"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.restaurant),
                    color = Orange3,
                    icon = "restaurant"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.family),
                    color = Red3Light,
                    icon = "family"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.social_life),
                    color = Blue2,
                    icon = "people"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.order_food),
                    color = Orange2,
                    icon = "orderfood2"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.travel),
                    color = BlueLight,
                    icon = "travel"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.fitness),
                    color = Purple2,
                    icon = "fitness"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.self_development),
                    color = Yellow,
                    icon = "selfdevelopment"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.clothes),
                    color = Green2Light,
                    icon = "clothes2"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.beauty),
                    color = Red3,
                    icon = "makeup"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.education),
                    color = Blue,
                    icon = "education"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.pet),
                    color = Orange3Light,
                    icon = "pet"
                ),

                CreateCategoryData(
                    name = stringRes(R.string.sports),
                    color = Purple1,
                    icon = "sports"
                ),
            )
        ).toImmutableList()
}

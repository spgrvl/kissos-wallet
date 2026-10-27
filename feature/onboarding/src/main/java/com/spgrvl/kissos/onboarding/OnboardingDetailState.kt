package com.spgrvl.kissos.onboarding

import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.legacy.data.model.AccountBalance
import com.spgrvl.kissos.wallet.domain.data.KissosCurrency
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateCategoryData
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class OnboardingDetailState(
    val currency: KissosCurrency,
    val accounts: ImmutableList<AccountBalance>,
    val accountSuggestions: ImmutableList<CreateAccountData>,
    val categories: ImmutableList<Category>,
    val categorySuggestions: ImmutableList<CreateCategoryData>
)

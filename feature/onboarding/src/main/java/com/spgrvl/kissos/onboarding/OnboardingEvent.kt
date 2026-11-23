package com.spgrvl.kissos.onboarding

import com.spgrvl.kissos.ui.AppLanguage
import com.spgrvl.kissos.data.model.Category
import com.spgrvl.kissos.legacy.datamodel.Account
import com.spgrvl.kissos.wallet.domain.data.KissosCurrency
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateCategoryData

sealed interface OnboardingEvent {

    data class SelectLanguage(val language: AppLanguage) : OnboardingEvent
    data class LanguageNext(val language: AppLanguage) : OnboardingEvent
    data object StartImport : OnboardingEvent
    data object ImportSkip : OnboardingEvent
    data class ImportFinished(val success: Boolean) : OnboardingEvent
    data object StartFresh : OnboardingEvent
    data class SetBaseCurrency(val baseCurrency: KissosCurrency) : OnboardingEvent
    data class EditAccount(val account: Account, val newBalance: Double) : OnboardingEvent
    data class CreateAccount(val data: CreateAccountData) : OnboardingEvent
    data object OnAddAccountsDone : OnboardingEvent
    data object OnAddAccountsSkip : OnboardingEvent
    data class EditCategory(val updatedCategory: Category) : OnboardingEvent
    data class CreateCategory(val data: CreateCategoryData) : OnboardingEvent
    data object OnAddCategoriesDone : OnboardingEvent
    data object OnAddCategoriesSkip : OnboardingEvent
}

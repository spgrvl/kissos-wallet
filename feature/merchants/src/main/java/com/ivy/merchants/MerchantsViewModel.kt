package com.ivy.merchants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Afm
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Merchant
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.MerchantRepository
import com.ivy.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Immutable
data class MerchantsState(
    val merchants: ImmutableList<MerchantUi>,
    val categories: ImmutableList<OptionUi>,
    val accounts: ImmutableList<OptionUi>,
)

@Immutable
data class MerchantUi(
    val afm: String,
    val displayName: String,
    val category: CategoryId?,
    val account: AccountId?,
    /** E.g. "Food · Revolut" */
    val defaults: String?,
)

/** A category or an account to pick. */
@Suppress("DataClassTypedIDs")
@Immutable
data class OptionUi(val id: UUID, val name: String, val color: Int)

sealed interface MerchantsEvent {
    data class Save(
        val afm: String,
        val displayName: String,
        val category: CategoryId?,
        val account: AccountId?,
    ) : MerchantsEvent

    data class Delete(val afm: String) : MerchantsEvent
}

@Stable
@HiltViewModel
class MerchantsViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val timeProvider: TimeProvider,
) : ComposeViewModel<MerchantsState, MerchantsEvent>() {
    private var merchants by mutableStateOf<List<Merchant>>(emptyList())
    private var categories by mutableStateOf<List<Category>>(emptyList())
    private var accounts by mutableStateOf<List<Account>>(emptyList())

    @Composable
    override fun uiState(): MerchantsState {
        LaunchedEffect(Unit) {
            categories = categoryRepository.findAll()
            accounts = accountRepository.findAll()
            merchants = merchantRepository.findAll()
        }

        return MerchantsState(
            merchants = merchants.map(::toUi).toImmutableList(),
            categories = categories.map { OptionUi(it.id.value, it.name.value, it.color.value) }
                .toImmutableList(),
            accounts = accounts.map { OptionUi(it.id.value, it.name.value, it.color.value) }
                .toImmutableList(),
        )
    }

    private fun toUi(merchant: Merchant): MerchantUi {
        val category = categories.find { it.id == merchant.defaultCategory }
        val account = accounts.find { it.id == merchant.defaultAccount }
        return MerchantUi(
            afm = merchant.afm.value,
            displayName = merchant.displayName.value,
            category = category?.id,
            account = account?.id,
            defaults = listOfNotNull(category?.name?.value, account?.name?.value)
                .joinToString(" · ")
                .ifEmpty { null },
        )
    }

    override fun onEvent(event: MerchantsEvent) {
        viewModelScope.launch {
            when (event) {
                is MerchantsEvent.Save -> save(event)
                is MerchantsEvent.Delete -> Afm.from(event.afm).onRight { merchantRepository.deleteByAfm(it) }
            }
            merchants = merchantRepository.findAll()
        }
    }

    private suspend fun save(event: MerchantsEvent.Save) {
        val afm = Afm.from(event.afm).getOrNull() ?: return
        val displayName = NotBlankTrimmedString.from(event.displayName).getOrNull() ?: return
        merchantRepository.save(
            Merchant(
                afm = afm,
                displayName = displayName,
                defaultCategory = event.category,
                defaultAccount = event.account,
                updatedAt = timeProvider.utcNow(),
            )
        )
    }
}

internal val previewState = MerchantsState(
    merchants = persistentListOf(
        MerchantUi("044136828", "Ψησταριά Παναγόπουλος", null, null, "Food · Revolut"),
        MerchantUi("067022059", "Barber", null, null, null),
    ),
    categories = persistentListOf(),
    accounts = persistentListOf(),
)

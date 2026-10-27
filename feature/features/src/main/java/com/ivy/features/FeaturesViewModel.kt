package com.ivy.features

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.ivy.domain.features.BoolFeature
import com.ivy.domain.features.FeatureGroup
import com.ivy.domain.features.Features
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class FeaturesViewModel @Inject constructor(
    private val features: Features,
    @ApplicationContext
    private val context: Context
) : ComposeViewModel<FeaturesUiState, FeaturesUiEvent>() {

    @Composable
    override fun uiState(): FeaturesUiState {
        return FeaturesUiState(
            featureItemViewStates = getFeatures()
        )
    }

    @SuppressLint("BuildListAdds")
    @Composable
    fun getFeatures(): ImmutableList<FeatureItemViewState> {
        return buildList {
            val groups: Map<FeatureGroup?, BoolFeature> =
                features.allFeatures
                    .associateBy { it.group }
                    .toSortedMap(compareBy { it?.name })

            groups.forEach { group ->
                add(
                    FeatureItemViewState.FeatureHeaderViewState(
                        name = groupName(group.key)
                    )
                )
                val featuresByGroup: List<FeatureItemViewState> = features
                    .allFeatures
                    .filter { it.group?.name == group.key?.name }
                    .map {
                        FeatureItemViewState.FeatureToggleViewState(
                            key = it.key,
                            name = featureName(it.key) ?: it.name ?: it.key,
                            description = featureDescription(it.key) ?: it.description,
                            enabled = it.asEnabledState()
                        )
                    }
                addAll(featuresByGroup)
            }
        }.toImmutableList()
    }

    override fun onEvent(event: FeaturesUiEvent) {
        when (event) {
            is FeaturesUiEvent.ToggleFeature -> toggleFeature(event)
        }
    }

    private fun toggleFeature(event: FeaturesUiEvent.ToggleFeature) {
        viewModelScope.launch {
            val feature = features.allFeatures.find { feature -> feature.key == event.key }
            val enabled = feature?.enabledFlow(context)?.first() ?: false
            feature?.set(context, !enabled)
        }
    }

    @Composable
    private fun groupName(group: FeatureGroup?): String = stringResource(
        when (group) {
            FeatureGroup.Category -> R.string.categories
            FeatureGroup.Account -> R.string.accounts
            FeatureGroup.Other, null -> R.string.other
        }
    )

    @Composable
    private fun featureName(key: String): String? = FEATURE_NAMES[key]?.let { stringResource(it) }

    @Composable
    private fun featureDescription(key: String): String? = FEATURE_DESCRIPTIONS[key]?.let { stringResource(it) }

    private companion object {
        /** Each feature's name in the app's language, by key. */
        val FEATURE_NAMES = mapOf(
            "sort_categories_ascending" to R.string.feature_sort_categories,
            "compact_account_ui" to R.string.feature_compact_accounts,
            "compact_category_ui" to R.string.feature_compact_categories,
            "show_title_suggestions" to R.string.feature_title_suggestions,
            "search_categories" to R.string.feature_search_categories,
            "hide_total_balance" to R.string.feature_hide_total_balance,
            "show_decimal_number" to R.string.feature_show_decimals,
            "enable_standard_keypad_layout" to R.string.feature_standard_keypad,
            "show_account_color" to R.string.feature_account_colors,
        )
        val FEATURE_DESCRIPTIONS = mapOf(
            "sort_categories_ascending" to R.string.feature_sort_categories_description,
            "compact_account_ui" to R.string.feature_compact_accounts_description,
            "compact_category_ui" to R.string.feature_compact_categories_description,
            "show_title_suggestions" to R.string.feature_title_suggestions_description,
            "search_categories" to R.string.feature_search_categories_description,
            "hide_total_balance" to R.string.feature_hide_total_balance_description,
            "show_decimal_number" to R.string.feature_show_decimals_description,
            "enable_standard_keypad_layout" to R.string.feature_standard_keypad_description,
            "show_account_color" to R.string.feature_account_colors_description,
        )
    }
}

package com.ivy.disclaimer

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList

data class DisclaimerViewState(
    val checkboxes: ImmutableList<CheckboxViewState>,
    val agreeButtonEnabled: Boolean,
)

data class CheckboxViewState(
    @StringRes val text: Int,
    val checked: Boolean
)

sealed interface DisclaimerViewEvent {
    data class OnCheckboxClick(val index: Int) : DisclaimerViewEvent
    data object OnAgreeClick : DisclaimerViewEvent
}
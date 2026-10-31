package com.spgrvl.kissos.disclaimer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.spgrvl.kissos.data.repository.LegalRepository
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val navigation: Navigation,
    private val legalRepo: LegalRepository,
) : ComposeViewModel<DisclaimerViewState, DisclaimerViewEvent>() {

    private var checkboxes by mutableStateOf(LegalCheckboxes)

    @Composable
    override fun uiState(): DisclaimerViewState {
        return DisclaimerViewState(
            checkboxes = checkboxes,
            agreeButtonEnabled = checkboxes.all(CheckboxViewState::checked),
        )
    }

    override fun onEvent(event: DisclaimerViewEvent) {
        when (event) {
            DisclaimerViewEvent.OnAgreeClick -> handleAgreeClick()
            is DisclaimerViewEvent.OnCheckboxClick -> handleCheckboxClick(event)
        }
    }

    private fun handleAgreeClick() {
        viewModelScope.launch {
            legalRepo.setDisclaimerAccepted(accepted = true)
            navigation.back()
        }
    }

    private fun handleCheckboxClick(event: DisclaimerViewEvent.OnCheckboxClick) {
        checkboxes = checkboxes.mapIndexed { index, item ->
            if (index == event.index) {
                item.copy(
                    checked = !item.checked
                )
            } else {
                item
            }
        }.toImmutableList()
    }

    companion object {
        val LegalCheckboxes = listOf(
            R.string.agreement_as_is,
            R.string.agreement_data,
            R.string.agreement_accuracy,
            R.string.agreement_liability,
        ).map { CheckboxViewState(text = it, checked = false) }
            .toImmutableList()
    }
}
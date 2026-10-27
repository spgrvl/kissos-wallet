package com.spgrvl.kissos.features

sealed interface FeaturesUiEvent {
    data class ToggleFeature(val key: String) : FeaturesUiEvent
}

package com.spgrvl.kissos.contributors

sealed interface ContributorsEvent {
    data object TryAgainButtonClicked : ContributorsEvent
}

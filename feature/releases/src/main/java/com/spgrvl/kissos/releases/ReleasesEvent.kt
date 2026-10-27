package com.spgrvl.kissos.releases

sealed interface ReleasesEvent {
    data object OnTryAgainClick : ReleasesEvent
}

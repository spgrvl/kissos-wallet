package com.spgrvl.kissos.accounts

sealed interface AccountsEvent {
    data class OnReorder(val reorderedList: List<com.spgrvl.kissos.legacy.data.model.AccountData>) :
        AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
}

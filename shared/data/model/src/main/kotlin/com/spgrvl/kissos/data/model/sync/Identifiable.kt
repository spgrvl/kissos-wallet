package com.spgrvl.kissos.data.model.sync

interface Identifiable<ID : UniqueId> {
    val id: ID
}

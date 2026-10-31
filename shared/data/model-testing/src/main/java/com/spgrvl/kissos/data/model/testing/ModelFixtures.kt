package com.spgrvl.kissos.data.model.testing

import com.spgrvl.kissos.data.model.AccountId
import com.spgrvl.kissos.data.model.CategoryId
import com.spgrvl.kissos.data.model.TransactionId
import java.util.UUID

object ModelFixtures {
    val AccountId = AccountId(UUID.randomUUID())
    val CategoryId = CategoryId(UUID.randomUUID())
    val TransactionId = TransactionId(UUID.randomUUID())
}

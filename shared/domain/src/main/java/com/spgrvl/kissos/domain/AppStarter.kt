package com.spgrvl.kissos.domain

import android.content.Intent
import com.spgrvl.kissos.base.model.TransactionType

/**
 * A component used to start the **RootActivity** without knowing about it.
 */
interface AppStarter {
    fun getRootIntent(): Intent
    fun defaultStartIntent(): Intent
    fun addTransactionStartIntent(type: TransactionType): Intent

    /** Opens a new expense with the receipt scanner already running. */
    fun scanReceiptStartIntent(): Intent
}

package com.spgrvl.kissos.wallet.migrations

interface Migration {
    val key: String

    suspend fun migrate()
}

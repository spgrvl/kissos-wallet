package com.spgrvl.kissos.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Tells a bank's account debit (e.g. Eurobank's, days after a card payment) from the payment itself. */
@Suppress("MagicNumber", "ClassNaming")
class Migration133to134_DetectedPaymentKind : Migration(133, 134) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `detected_payments` ADD COLUMN `kind` TEXT NOT NULL DEFAULT 'PAYMENT'")
        db.execSQL("ALTER TABLE `detected_payments` ADD COLUMN `matchedPaymentId` TEXT")
    }
}

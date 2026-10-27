package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Receipts sent to myDATA (e.g. through e-invoicing providers) have no cash register, receipt
 * number or amount until AADE is asked, so those become nullable and receipts are unique by `sig`.
 */
@Suppress("MagicNumber", "ClassNaming")
class Migration131to132_MyDataReceipts : Migration(131, 132) {
    @Suppress("MaximumLineLength", "MaxLineLength")
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `receipts_new` (`transactionId` TEXT NOT NULL, `sig` TEXT NOT NULL, `registerId` TEXT, `receiptNumber` INTEGER, `mark` INTEGER, `total` REAL, `issuedAt` INTEGER, `merchantAfm` TEXT, `merchantName` TEXT, `address` TEXT, `documentType` TEXT, `cardAmount` REAL, `netA` REAL, `vatA` REAL, `netB` REAL, `vatB` REAL, `netC` REAL, `vatC` REAL, `netD` REAL, `vatD` REAL, `netE` REAL, `vatE` REAL, `status` TEXT NOT NULL, `aadeMessage` TEXT, `scannedAt` INTEGER NOT NULL, `lastCheckedAt` INTEGER, PRIMARY KEY(`transactionId`), FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        val columns = "`transactionId`, `sig`, `registerId`, `receiptNumber`, `total`, `issuedAt`, " +
            "`merchantAfm`, `merchantName`, `address`, `documentType`, `cardAmount`, `netA`, `vatA`, " +
            "`netB`, `vatB`, `netC`, `vatC`, `netD`, `vatD`, `netE`, `vatE`, `status`, `aadeMessage`, " +
            "`scannedAt`, `lastCheckedAt`"
        db.execSQL("INSERT INTO `receipts_new` ($columns) SELECT $columns FROM `receipts`")
        db.execSQL("DROP TABLE `receipts`")
        db.execSQL("ALTER TABLE `receipts_new` RENAME TO `receipts`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_receipts_sig` ON `receipts` (`sig`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_receipts_mark` ON `receipts` (`mark`)")
    }
}

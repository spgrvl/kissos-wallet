package com.spgrvl.kissos.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("MagicNumber", "ClassNaming")
class Migration130to131_Receipts : Migration(130, 131) {
    @Suppress("MaximumLineLength", "MaxLineLength")
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `receipts` (`transactionId` TEXT NOT NULL, `sig` TEXT NOT NULL, `registerId` TEXT NOT NULL, `receiptNumber` INTEGER NOT NULL, `total` REAL NOT NULL, `issuedAt` INTEGER, `merchantAfm` TEXT, `merchantName` TEXT, `address` TEXT, `documentType` TEXT, `cardAmount` REAL, `netA` REAL, `vatA` REAL, `netB` REAL, `vatB` REAL, `netC` REAL, `vatC` REAL, `netD` REAL, `vatD` REAL, `netE` REAL, `vatE` REAL, `status` TEXT NOT NULL, `aadeMessage` TEXT, `scannedAt` INTEGER NOT NULL, `lastCheckedAt` INTEGER, PRIMARY KEY(`transactionId`), FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_receipts_registerId_receiptNumber` ON `receipts` (`registerId`, `receiptNumber`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `merchants` (`afm` TEXT NOT NULL, `displayName` TEXT NOT NULL, `defaultCategoryId` TEXT, `defaultAccountId` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`afm`))"
        )
    }
}

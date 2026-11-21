package com.spgrvl.kissos.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Payments detected in bank apps' notifications, and what's learned from the user's choices about them. */
@Suppress("MagicNumber", "ClassNaming")
class Migration132to133_BankNotifications : Migration(132, 133) {
    @Suppress("MaximumLineLength", "MaxLineLength")
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `detected_payments` (`id` TEXT NOT NULL, `dedupKey` TEXT NOT NULL, `sourcePackage` TEXT NOT NULL, `postedAt` INTEGER NOT NULL, `amount` REAL NOT NULL, `currency` TEXT NOT NULL, `direction` TEXT NOT NULL, `payee` TEXT, `categoryHint` TEXT, `status` TEXT NOT NULL, `transactionId` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_detected_payments_dedupKey` ON `detected_payments` (`dedupKey`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `payee_memory` (`payee` TEXT NOT NULL, `title` TEXT NOT NULL, `categoryId` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`payee`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `category_hint_memory` (`sourcePackage` TEXT NOT NULL, `hint` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`sourcePackage`, `hint`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `unparsed_notifications` (`id` TEXT NOT NULL, `sourcePackage` TEXT NOT NULL, `title` TEXT, `text` TEXT NOT NULL, `postedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}

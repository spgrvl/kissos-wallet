package com.spgrvl.kissos.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.spgrvl.kissos.data.db.migration.Migration123to124_LoanIncludeDateTime
import com.spgrvl.kissos.data.db.migration.Migration124to125_LoanEditDateTime
import com.spgrvl.kissos.data.db.migration.Migration126to127_LoanRecordType
import com.spgrvl.kissos.data.db.migration.Migration129to130_LoanIncludeNote
import com.spgrvl.kissos.data.db.migration.Migration130to131_Receipts
import com.spgrvl.kissos.data.db.migration.Migration131to132_MyDataReceipts
import com.spgrvl.kissos.data.model.LoanType
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class KissosRoomDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KissosRoomDatabase::class.java,
        listOf(KissosRoomDatabase.DeleteSEMigration()),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate131to132_MyDataReceipts() {
        // given
        val transactionId = UUID.randomUUID().toString()
        val myDataTransactionId = UUID.randomUUID().toString()
        helper.createDatabase(TestDb, 131).apply {
            listOf(transactionId, myDataTransactionId).forEach { id ->
                execSQL(
                    "INSERT INTO transactions (accountId, type, amount, isSynced, isDeleted, id) " +
                        "VALUES ('${UUID.randomUUID()}', 'EXPENSE', 12.0, 0, 0, '$id')"
                )
            }
            execSQL(
                "INSERT INTO receipts (transactionId, sig, registerId, receiptNumber, total, merchantAfm, " +
                    "netE, status, scannedAt) " +
                    "VALUES ('$transactionId', 'SIG', 'DNX25002295', 595, 12.0, '067022059', 12.0, 'FOUND', 7)"
            )
            close()
        }

        // when
        val newDb = helper.runMigrationsAndValidate(
            TestDb,
            132,
            true,
            Migration131to132_MyDataReceipts()
        )

        // then: existing receipts are kept
        newDb.query(
            "SELECT sig, registerId, receiptNumber, mark, total, merchantAfm, netE, status, scannedAt FROM receipts"
        ).apply {
            moveToFirst() shouldBe true
            getString(0) shouldBe "SIG"
            getString(1) shouldBe "DNX25002295"
            getLong(2) shouldBe 595
            isNull(3) shouldBe true
            getDouble(4) shouldBe 12.0
            getString(5) shouldBe "067022059"
            getDouble(6) shouldBe 12.0
            getString(7) shouldBe "FOUND"
            getLong(8) shouldBe 7
        }

        // and: a myDATA receipt needs only its link until AADE is asked
        newDb.execSQL(
            "INSERT INTO receipts (transactionId, sig, status, scannedAt) " +
                "VALUES ('$myDataTransactionId', 'https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo?q=1', " +
                "'PENDING', 0)"
        )

        // and: receipts are still deleted together with their transaction
        newDb.execSQL("PRAGMA foreign_keys = ON")
        newDb.execSQL("DELETE FROM transactions WHERE id = '$transactionId'")
        newDb.query("SELECT COUNT(*) FROM receipts").apply {
            moveToFirst()
            getInt(0) shouldBe 1
        }
        newDb.close()
    }

    @Test
    fun migrate130to131_Receipts() {
        // given
        val transactionId = UUID.randomUUID().toString()
        helper.createDatabase(TestDb, 130).apply {
            execSQL(
                "INSERT INTO transactions (accountId, type, amount, title, isSynced, isDeleted, id) " +
                    "VALUES ('${UUID.randomUUID()}', 'EXPENSE', 12.0, 'Barber', 0, 0, '$transactionId')"
            )
            close()
        }

        // when
        val newDb = helper.runMigrationsAndValidate(
            TestDb,
            131,
            true,
            Migration130to131_Receipts()
        )

        // then: existing data is kept and receipts can be attached to transactions
        newDb.query("SELECT title FROM transactions WHERE id = ?", arrayOf(transactionId)).apply {
            moveToFirst() shouldBe true
            getString(0) shouldBe "Barber"
        }
        newDb.execSQL(
            "INSERT INTO receipts (transactionId, sig, registerId, receiptNumber, total, status, scannedAt) " +
                "VALUES ('$transactionId', 'SIG', 'DNX25002295', 595, 12.0, 'PENDING', 0)"
        )
        newDb.execSQL("INSERT INTO merchants (afm, displayName, updatedAt) VALUES ('067022059', 'Barber', 0)")

        // and: a receipt is deleted together with its transaction
        newDb.execSQL("PRAGMA foreign_keys = ON")
        newDb.execSQL("DELETE FROM transactions WHERE id = '$transactionId'")
        newDb.query("SELECT COUNT(*) FROM receipts").apply {
            moveToFirst()
            getInt(0) shouldBe 0
        }
        newDb.query("SELECT COUNT(*) FROM merchants").apply {
            moveToFirst()
            getInt(0) shouldBe 1
        }
        newDb.close()
    }

    @Test
    fun migrate129to130_LoanIncludeNote() {
        helper.createDatabase(TestDb, 129).apply {
            val insertSql = """
                INSERT INTO loans (name, amount, type, color, icon, orderNum, accountId, isSynced, isDeleted, dateTime, id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent()

            val preparedStatement = compileStatement(insertSql).apply {
                // Bind values
                bindString(1, "Loan 1")
                bindDouble(2, 123.50)
                bindString(3, LoanType.BORROW.name) // Assuming you store enum as name
                bindLong(4, 13)
                bindString(5, "ic")
                bindDouble(6, 3.14)
                bindString(7, UUID.randomUUID().toString())
                bindLong(8, 1)
                bindLong(9, 0)
                bindString(10, "")
                bindString(11, UUID.randomUUID().toString())
            }
            preparedStatement.executeInsert()
            close()
        }

        val newDb = helper.runMigrationsAndValidate(
            TestDb,
            130,
            true,
            Migration129to130_LoanIncludeNote()
        )

        newDb.query("SELECT * FROM loans").apply {
            moveToFirst() shouldBe true
            getString(0) shouldBe "Loan 1"
            getDouble(1) shouldBe 123.50
            getString(2) shouldBe LoanType.BORROW.name
        }
        newDb.close()
    }

    @Test
    fun migrate123to125_LoanDateTime() {
        // given
        helper.createDatabase(TestDb, 123).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            val insertSql = """
                INSERT INTO loans (name, amount, type, color, icon, orderNum, accountId, isSynced, isDeleted, id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent()

            // Assuming you have an instance of LoanEntity named loanEntity
            val preparedStatement = compileStatement(insertSql).apply {
                // Bind the values from your LoanEntity instance to the prepared statement
                bindString(1, "Loan 1")
                bindDouble(2, 123.50)
                bindString(3, LoanType.BORROW.name) // Assuming you store enum as name
                bindLong(4, 13)
                bindString(5, "ic")
                bindDouble(6, 3.14)
                bindString(7, UUID.randomUUID().toString())
                bindLong(8, 1)
                bindLong(9, 0)
                bindString(10, UUID.randomUUID().toString())
            }
            preparedStatement.executeInsert()
            close()
        }

        // when
        helper.runMigrationsAndValidate(
            TestDb,
            124,
            true,
            Migration123to124_LoanIncludeDateTime()
        )
        val newDb = helper.runMigrationsAndValidate(
            TestDb,
            125,
            true,
            Migration124to125_LoanEditDateTime()
        )

        // then
        newDb.query("SELECT * FROM loans").apply {
            moveToFirst() shouldBe true
            getString(0) shouldBe "Loan 1"
            getDouble(1) shouldBe 123.50
            getString(2) shouldBe LoanType.BORROW.name
        }
        newDb.close()
    }

    @Test
    fun migrate126to127_LoanRecordType() {
        // given
        val loanId = UUID.randomUUID().toString()
        val noteString = "here is your note"
        helper.createDatabase(TestDb, 126).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            val insertSql = """
                INSERT INTO loan_records (loanId, amount, note, dateTime, interest, accountId, convertedAmount, isSynced, isDeleted, id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent()
            // Assuming you have an instance of LoanRecordEntity named loanRecordEntity
            val preparedStatement = compileStatement(insertSql).apply {
                // Bind the values from your LoanRecordEntity instance to the prepared statement
                bindString(1, loanId)
                bindDouble(2, 123.50)
                bindString(3, noteString)
                bindString(4, "this will fail, LocalDateTimeNeeded")
                bindLong(5, 0) // interest
                bindString(6, UUID.randomUUID().toString())
                bindDouble(7, 3.14) // convertedAmount
                bindLong(8, 1)
                bindLong(9, 0)
                bindString(10, UUID.randomUUID().toString())
            }
            preparedStatement.executeInsert()
            close()
        }

        // when
        val newDb = helper.runMigrationsAndValidate(
            TestDb,
            127,
            true,
            Migration126to127_LoanRecordType()
        )

        // then
        newDb.query("SELECT * FROM loan_records").apply {
            moveToFirst() shouldBe true
            getString(0) shouldBe loanId
            getDouble(1) shouldBe 123.50
            getString(2) shouldBe noteString
            getString(10) shouldBe "DECREASE"
        }
        newDb.close()
    }

    @Test
    fun migrateAll() {
        // given:
        // Create earliest version of the database:
        // for Ivy Wallet versions below 106 are broken :/
        helper.createDatabase(TestDb, 106).apply {
            close()
        }

        // then:
        // Open latest version of the database.
        // Room validates and executes all migrations.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            KissosRoomDatabase::class.java,
            TestDb
        ).addMigrations(*KissosRoomDatabase.migrations()).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    companion object {
        private const val TestDb = "migration-test"
    }
}
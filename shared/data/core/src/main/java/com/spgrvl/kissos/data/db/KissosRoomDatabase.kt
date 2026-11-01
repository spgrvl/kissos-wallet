package com.spgrvl.kissos.data.db

import android.content.Context
import androidx.room.*
import com.spgrvl.kissos.data.db.dao.read.AccountDao
import com.spgrvl.kissos.data.db.dao.read.BudgetDao
import com.spgrvl.kissos.data.db.dao.read.CategoryDao
import com.spgrvl.kissos.data.db.dao.read.ExchangeRatesDao
import com.spgrvl.kissos.data.db.dao.read.LoanDao
import com.spgrvl.kissos.data.db.dao.read.LoanRecordDao
import com.spgrvl.kissos.data.db.dao.read.MerchantDao
import com.spgrvl.kissos.data.db.dao.read.PlannedPaymentRuleDao
import com.spgrvl.kissos.data.db.dao.read.ReceiptDao
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.data.db.dao.read.TagAssociationDao
import com.spgrvl.kissos.data.db.dao.read.TagDao
import com.spgrvl.kissos.data.db.dao.read.TransactionDao
import com.spgrvl.kissos.data.db.dao.read.UserDao
import com.spgrvl.kissos.data.db.dao.write.WriteAccountDao
import com.spgrvl.kissos.data.db.dao.write.WriteBudgetDao
import com.spgrvl.kissos.data.db.dao.write.WriteCategoryDao
import com.spgrvl.kissos.data.db.dao.write.WriteExchangeRatesDao
import com.spgrvl.kissos.data.db.dao.write.WriteLoanDao
import com.spgrvl.kissos.data.db.dao.write.WriteLoanRecordDao
import com.spgrvl.kissos.data.db.dao.write.WriteMerchantDao
import com.spgrvl.kissos.data.db.dao.write.WritePlannedPaymentRuleDao
import com.spgrvl.kissos.data.db.dao.write.WriteReceiptDao
import com.spgrvl.kissos.data.db.dao.write.WriteSettingsDao
import com.spgrvl.kissos.data.db.dao.write.WriteTagAssociationDao
import com.spgrvl.kissos.data.db.dao.write.WriteTagDao
import com.spgrvl.kissos.data.db.dao.write.WriteTransactionDao
import com.spgrvl.kissos.data.db.entity.AccountEntity
import com.spgrvl.kissos.data.db.entity.BudgetEntity
import com.spgrvl.kissos.data.db.entity.CategoryEntity
import com.spgrvl.kissos.data.db.entity.ExchangeRateEntity
import com.spgrvl.kissos.data.db.entity.LoanEntity
import com.spgrvl.kissos.data.db.entity.LoanRecordEntity
import com.spgrvl.kissos.data.db.entity.MerchantEntity
import com.spgrvl.kissos.data.db.entity.PlannedPaymentRuleEntity
import com.spgrvl.kissos.data.db.entity.ReceiptEntity
import com.spgrvl.kissos.data.db.entity.SettingsEntity
import com.spgrvl.kissos.data.db.entity.TagAssociationEntity
import com.spgrvl.kissos.data.db.entity.TagEntity
import com.spgrvl.kissos.data.db.entity.TransactionEntity
import com.spgrvl.kissos.data.db.entity.UserEntity
import com.spgrvl.kissos.data.db.migration.Migration130to131_Receipts
import com.spgrvl.kissos.data.db.migration.Migration131to132_MyDataReceipts
import com.spgrvl.kissos.domain.db.RoomTypeConverters

@Database(
    entities = [
        AccountEntity::class, TransactionEntity::class, CategoryEntity::class,
        SettingsEntity::class, PlannedPaymentRuleEntity::class,
        UserEntity::class, ExchangeRateEntity::class, BudgetEntity::class,
        LoanEntity::class, LoanRecordEntity::class, TagEntity::class, TagAssociationEntity::class,
        ReceiptEntity::class, MerchantEntity::class
    ],
    version = 132,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class KissosRoomDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val budgetDao: BudgetDao
    abstract val plannedPaymentRuleDao: PlannedPaymentRuleDao
    abstract val settingsDao: SettingsDao
    abstract val userDao: UserDao
    abstract val exchangeRatesDao: ExchangeRatesDao
    abstract val loanDao: LoanDao
    abstract val loanRecordDao: LoanRecordDao
    abstract val tagDao: TagDao
    abstract val tagAssociationDao: TagAssociationDao
    abstract val receiptDao: ReceiptDao
    abstract val merchantDao: MerchantDao

    abstract val writeAccountDao: WriteAccountDao
    abstract val writeTransactionDao: WriteTransactionDao
    abstract val writeCategoryDao: WriteCategoryDao
    abstract val writeBudgetDao: WriteBudgetDao
    abstract val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao
    abstract val writeSettingsDao: WriteSettingsDao
    abstract val writeExchangeRatesDao: WriteExchangeRatesDao
    abstract val writeLoanDao: WriteLoanDao
    abstract val writeLoanRecordDao: WriteLoanRecordDao
    abstract val writeTagDao: WriteTagDao
    abstract val writeTagAssociationDao: WriteTagAssociationDao
    abstract val writeReceiptDao: WriteReceiptDao
    abstract val writeMerchantDao: WriteMerchantDao

    companion object {
        const val DB_NAME = "kissos.db"

        fun migrations() = arrayOf(
            Migration130to131_Receipts(),
            Migration131to132_MyDataReceipts()
        )

        @Suppress("SpreadOperator")
        fun create(applicationContext: Context): KissosRoomDatabase {
            return Room
                .databaseBuilder(
                    applicationContext,
                    KissosRoomDatabase::class.java,
                    DB_NAME
                )
                .addMigrations(*migrations())
                .build()
        }
    }

}

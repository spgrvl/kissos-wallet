package com.spgrvl.kissos.data.di

import android.content.Context
import com.spgrvl.kissos.data.db.KissosRoomDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomDbModule {

    @Provides
    @Singleton
    fun provideKissosRoomDatabase(
        @ApplicationContext appContext: Context,
    ): KissosRoomDatabase {
        return KissosRoomDatabase.create(
            applicationContext = appContext,
        )
    }

    @Provides
    fun provideUserDao(db: KissosRoomDatabase): UserDao {
        return db.userDao
    }

    @Provides
    fun provideAccountDao(db: KissosRoomDatabase): AccountDao {
        return db.accountDao
    }

    @Provides
    fun provideTransactionDao(db: KissosRoomDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    fun provideCategoryDao(db: KissosRoomDatabase): CategoryDao {
        return db.categoryDao
    }

    @Provides
    fun provideBudgetDao(db: KissosRoomDatabase): BudgetDao {
        return db.budgetDao
    }

    @Provides
    fun provideSettingsDao(db: KissosRoomDatabase): SettingsDao {
        return db.settingsDao
    }

    @Provides
    fun provideLoanDao(db: KissosRoomDatabase): LoanDao {
        return db.loanDao
    }

    @Provides
    fun provideLoanRecordDao(db: KissosRoomDatabase): LoanRecordDao {
        return db.loanRecordDao
    }

    @Provides
    fun providePlannedPaymentRuleDao(db: KissosRoomDatabase): PlannedPaymentRuleDao {
        return db.plannedPaymentRuleDao
    }

    @Provides
    fun provideTagDao(db: KissosRoomDatabase): TagDao {
        return db.tagDao
    }

    @Provides
    fun provideTagAssociationDao(db: KissosRoomDatabase): TagAssociationDao {
        return db.tagAssociationDao
    }

    @Provides
    fun provideExchangeRatesDao(
        roomDatabase: KissosRoomDatabase
    ): ExchangeRatesDao {
        return roomDatabase.exchangeRatesDao
    }

    @Provides
    fun provideWriteAccountDao(db: KissosRoomDatabase): WriteAccountDao {
        return db.writeAccountDao
    }

    @Provides
    fun provideWriteTransactionDao(db: KissosRoomDatabase): WriteTransactionDao {
        return db.writeTransactionDao
    }

    @Provides
    fun provideWriteCategoryDao(db: KissosRoomDatabase): WriteCategoryDao {
        return db.writeCategoryDao
    }

    @Provides
    fun provideWriteBudgetDao(db: KissosRoomDatabase): WriteBudgetDao {
        return db.writeBudgetDao
    }

    @Provides
    fun provideWriteSettingsDao(db: KissosRoomDatabase): WriteSettingsDao {
        return db.writeSettingsDao
    }

    @Provides
    fun provideWriteLoanDao(db: KissosRoomDatabase): WriteLoanDao {
        return db.writeLoanDao
    }

    @Provides
    fun provideWriteLoanRecordDao(db: KissosRoomDatabase): WriteLoanRecordDao {
        return db.writeLoanRecordDao
    }

    @Provides
    fun provideWritePlannedPaymentRuleDao(db: KissosRoomDatabase): WritePlannedPaymentRuleDao {
        return db.writePlannedPaymentRuleDao
    }

    @Provides
    fun provideWriteExchangeRatesDao(db: KissosRoomDatabase): WriteExchangeRatesDao {
        return db.writeExchangeRatesDao
    }

    @Provides
    fun provideWriteTagDao(db: KissosRoomDatabase): WriteTagDao {
        return db.writeTagDao
    }

    @Provides
    fun provideWriteTagAssociationDao(db: KissosRoomDatabase): WriteTagAssociationDao {
        return db.writeTagAssociationDao
    }

    @Provides
    fun provideReceiptDao(db: KissosRoomDatabase): ReceiptDao {
        return db.receiptDao
    }

    @Provides
    fun provideMerchantDao(db: KissosRoomDatabase): MerchantDao {
        return db.merchantDao
    }

    @Provides
    fun provideWriteReceiptDao(db: KissosRoomDatabase): WriteReceiptDao {
        return db.writeReceiptDao
    }

    @Provides
    fun provideWriteMerchantDao(db: KissosRoomDatabase): WriteMerchantDao {
        return db.writeMerchantDao
    }
}

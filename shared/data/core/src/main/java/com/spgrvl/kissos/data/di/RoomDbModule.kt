package com.spgrvl.kissos.data.di

import android.content.Context
import com.spgrvl.kissos.data.db.IvyRoomDatabase
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
    fun provideIvyRoomDatabase(
        @ApplicationContext appContext: Context,
    ): IvyRoomDatabase {
        return IvyRoomDatabase.create(
            applicationContext = appContext,
        )
    }

    @Provides
    fun provideUserDao(db: IvyRoomDatabase): UserDao {
        return db.userDao
    }

    @Provides
    fun provideAccountDao(db: IvyRoomDatabase): AccountDao {
        return db.accountDao
    }

    @Provides
    fun provideTransactionDao(db: IvyRoomDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    fun provideCategoryDao(db: IvyRoomDatabase): CategoryDao {
        return db.categoryDao
    }

    @Provides
    fun provideBudgetDao(db: IvyRoomDatabase): BudgetDao {
        return db.budgetDao
    }

    @Provides
    fun provideSettingsDao(db: IvyRoomDatabase): SettingsDao {
        return db.settingsDao
    }

    @Provides
    fun provideLoanDao(db: IvyRoomDatabase): LoanDao {
        return db.loanDao
    }

    @Provides
    fun provideLoanRecordDao(db: IvyRoomDatabase): LoanRecordDao {
        return db.loanRecordDao
    }

    @Provides
    fun providePlannedPaymentRuleDao(db: IvyRoomDatabase): PlannedPaymentRuleDao {
        return db.plannedPaymentRuleDao
    }

    @Provides
    fun provideTagDao(db: IvyRoomDatabase): TagDao {
        return db.tagDao
    }

    @Provides
    fun provideTagAssociationDao(db: IvyRoomDatabase): TagAssociationDao {
        return db.tagAssociationDao
    }

    @Provides
    fun provideExchangeRatesDao(
        roomDatabase: IvyRoomDatabase
    ): ExchangeRatesDao {
        return roomDatabase.exchangeRatesDao
    }

    @Provides
    fun provideWriteAccountDao(db: IvyRoomDatabase): WriteAccountDao {
        return db.writeAccountDao
    }

    @Provides
    fun provideWriteTransactionDao(db: IvyRoomDatabase): WriteTransactionDao {
        return db.writeTransactionDao
    }

    @Provides
    fun provideWriteCategoryDao(db: IvyRoomDatabase): WriteCategoryDao {
        return db.writeCategoryDao
    }

    @Provides
    fun provideWriteBudgetDao(db: IvyRoomDatabase): WriteBudgetDao {
        return db.writeBudgetDao
    }

    @Provides
    fun provideWriteSettingsDao(db: IvyRoomDatabase): WriteSettingsDao {
        return db.writeSettingsDao
    }

    @Provides
    fun provideWriteLoanDao(db: IvyRoomDatabase): WriteLoanDao {
        return db.writeLoanDao
    }

    @Provides
    fun provideWriteLoanRecordDao(db: IvyRoomDatabase): WriteLoanRecordDao {
        return db.writeLoanRecordDao
    }

    @Provides
    fun provideWritePlannedPaymentRuleDao(db: IvyRoomDatabase): WritePlannedPaymentRuleDao {
        return db.writePlannedPaymentRuleDao
    }

    @Provides
    fun provideWriteExchangeRatesDao(db: IvyRoomDatabase): WriteExchangeRatesDao {
        return db.writeExchangeRatesDao
    }

    @Provides
    fun provideWriteTagDao(db: IvyRoomDatabase): WriteTagDao {
        return db.writeTagDao
    }

    @Provides
    fun provideWriteTagAssociationDao(db: IvyRoomDatabase): WriteTagAssociationDao {
        return db.writeTagAssociationDao
    }

    @Provides
    fun provideReceiptDao(db: IvyRoomDatabase): ReceiptDao {
        return db.receiptDao
    }

    @Provides
    fun provideMerchantDao(db: IvyRoomDatabase): MerchantDao {
        return db.merchantDao
    }

    @Provides
    fun provideWriteReceiptDao(db: IvyRoomDatabase): WriteReceiptDao {
        return db.writeReceiptDao
    }

    @Provides
    fun provideWriteMerchantDao(db: IvyRoomDatabase): WriteMerchantDao {
        return db.writeMerchantDao
    }
}

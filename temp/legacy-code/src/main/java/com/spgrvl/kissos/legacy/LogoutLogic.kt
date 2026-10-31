package com.spgrvl.kissos.legacy

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.spgrvl.kissos.base.legacy.SharedPrefs
import com.spgrvl.kissos.data.DataObserver
import com.spgrvl.kissos.data.DataWriteEvent
import com.spgrvl.kissos.data.db.dao.read.UserDao
import com.spgrvl.kissos.data.db.dao.write.WriteBudgetDao
import com.spgrvl.kissos.data.db.dao.write.WriteLoanDao
import com.spgrvl.kissos.data.db.dao.write.WriteLoanRecordDao
import com.spgrvl.kissos.data.db.dao.write.WriteMerchantDao
import com.spgrvl.kissos.data.db.dao.write.WritePlannedPaymentRuleDao
import com.spgrvl.kissos.data.db.dao.write.WriteReceiptDao
import com.spgrvl.kissos.data.db.dao.write.WriteSettingsDao
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.CategoryRepository
import com.spgrvl.kissos.data.repository.ExchangeRatesRepository
import com.spgrvl.kissos.data.repository.TagRepository
import com.spgrvl.kissos.data.repository.TransactionRepository
import com.spgrvl.kissos.legacy.utils.ioThread
import com.spgrvl.kissos.navigation.MainScreen
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.navigation.OnboardingScreen
import javax.inject.Inject

@Deprecated("Migrate to an UseCase in the domain layer.")
class LogoutLogic @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val navigation: Navigation,
    private val dataObserver: DataObserver,
    private val dataStore: DataStore<Preferences>,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val userDao: UserDao,
    private val writeSettingsDao: WriteSettingsDao,
    private val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao,
    private val writeBudgetDao: WriteBudgetDao,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val writeReceiptDao: WriteReceiptDao,
    private val writeMerchantDao: WriteMerchantDao,
) {
    suspend fun logout() {
        ioThread {
            deleteAllData()
            dataStore.edit {
                it.clear()
            }
            sharedPrefs.removeAll()
        }

        dataObserver.post(DataWriteEvent.AllDataChange)
        navigation.resetBackStack()
        navigation.navigateTo(OnboardingScreen)
    }

    private suspend fun deleteAllData() {
        writeReceiptDao.deleteAll()
        writeMerchantDao.deleteAll()
        accountRepository.deleteAll()
        transactionRepository.deleteAll()
        categoryRepository.deleteAll()
        tagRepository.deleteAll()
        writeSettingsDao.deleteAll()
        writePlannedPaymentRuleDao.deleteAll()
        userDao.deleteAll()
        writeBudgetDao.deleteAll()
        writeLoanDao.deleteAll()
        writeLoanRecordDao.deleteAll()
        exchangeRatesRepository.deleteAll()
    }

    suspend fun cloudLogout() {
        navigation.navigateTo(MainScreen)
    }
}

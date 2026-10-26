package com.ivy.data.backup

import com.ivy.base.TestDispatchersProvider
import com.ivy.base.di.KotlinxSerializationModule
import com.ivy.data.DataObserver
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakeBudgetDao
import com.ivy.data.db.dao.fake.FakeCategoryDao
import com.ivy.data.db.dao.fake.FakeLoanDao
import com.ivy.data.db.dao.fake.FakeLoanRecordDao
import com.ivy.data.db.dao.fake.FakeMerchantDao
import com.ivy.data.db.dao.fake.FakePlannedPaymentDao
import com.ivy.data.db.dao.fake.FakeReceiptDao
import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.dao.fake.FakeTagAssociationDao
import com.ivy.data.db.dao.fake.FakeTagDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.MerchantEntity
import com.ivy.data.db.entity.ReceiptEntity
import com.ivy.data.model.ReceiptStatus
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.fake.fakeRepositoryMemoFactory
import com.ivy.data.repository.mapper.AccountMapper
import com.ivy.data.testResource
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class BackupDataUseCaseTest {
    private fun newBackupDataUseCase(
        accountDao: FakeAccountDao = FakeAccountDao(),
        categoryDao: FakeCategoryDao = FakeCategoryDao(),
        transactionDao: FakeTransactionDao = FakeTransactionDao(),
        plannedPaymentDao: FakePlannedPaymentDao = FakePlannedPaymentDao(),
        budgetDao: FakeBudgetDao = FakeBudgetDao(),
        settingsDao: FakeSettingsDao = FakeSettingsDao(),
        loanDao: FakeLoanDao = FakeLoanDao(),
        loanRecordDao: FakeLoanRecordDao = FakeLoanRecordDao(),
        tagDao: FakeTagDao = FakeTagDao(),
        tagAssociationDao: FakeTagAssociationDao = FakeTagAssociationDao(),
        receiptDao: FakeReceiptDao = FakeReceiptDao(),
        merchantDao: FakeMerchantDao = FakeMerchantDao(),
    ): BackupDataUseCase {
        val accountMapper = AccountMapper(
            CurrencyRepository(
                settingsDao = settingsDao,
                writeSettingsDao = settingsDao,
                dispatchersProvider = TestDispatchersProvider,
            )
        )
        return BackupDataUseCase(
            accountDao = accountDao,
            accountMapper = accountMapper,
            accountRepository = AccountRepository(
                accountDao = accountDao,
                writeAccountDao = accountDao,
                mapper = accountMapper,
                dispatchersProvider = TestDispatchersProvider,
                memoFactory = fakeRepositoryMemoFactory(),
            ),
            budgetDao = budgetDao,
            categoryDao = categoryDao,
            loanRecordDao = loanRecordDao,
            loanDao = loanDao,
            plannedPaymentRuleDao = plannedPaymentDao,
            transactionDao = transactionDao,
            transactionWriter = transactionDao,
            settingsDao = settingsDao,
            categoryWriter = categoryDao,
            settingsWriter = settingsDao,
            budgetWriter = budgetDao,
            loanWriter = loanDao,
            loanRecordWriter = loanRecordDao,
            plannedPaymentRuleWriter = plannedPaymentDao,

            context = mockk(relaxed = true),
            sharedPrefs = mockk(relaxed = true),
            json = KotlinxSerializationModule.provideJson(),
            dispatchersProvider = TestDispatchersProvider,
            fileSystem = mockk(relaxed = true),
            dataObserver = DataObserver(),
            tagsReader = tagDao,
            tagsWriter = tagDao,
            tagAssociationReader = tagAssociationDao,
            tagAssociationWriter = tagAssociationDao,
            receiptReader = receiptDao,
            receiptWriter = receiptDao,
            merchantReader = merchantDao,
            merchantWriter = merchantDao,
        )
    }

    private suspend fun backupTestCase(backupVersion: String) {
        // given
        val originalBackupUseCase = newBackupDataUseCase()
        val backupJsonData = testResource("backups/$backupVersion.json")
            .readText(Charsets.UTF_16)

        // when
        val importedDataRes = originalBackupUseCase.importJson(backupJsonData, onProgress = {})

        // then
        importedDataRes.accountsImported shouldBeGreaterThan 0
        importedDataRes.transactionsImported shouldBeGreaterThan 0
        importedDataRes.categoriesImported shouldBeGreaterThan 0
        importedDataRes.failedRows.size shouldBe 0

        // Also - exporting and re-importing the data should work
        // given
        val exportedJson = originalBackupUseCase.generateJsonBackup()

        // when
        val freshBackupUseCase = newBackupDataUseCase()
        val reImportedDataRes = freshBackupUseCase.importJson(exportedJson, onProgress = {})
        // then
        reImportedDataRes shouldBe importedDataRes

        // Finally, exporting again should yield the same result
        freshBackupUseCase.generateJsonBackup() shouldBe exportedJson
    }

    @Test
    fun `backup compatibility with 450 (150)`() = runTest {
        backupTestCase("450-150")
    }

    @Test
    fun `receipts and merchants survive a backup round trip`() = runTest {
        // given
        val transactionDao = FakeTransactionDao()
        val receiptDao = FakeReceiptDao()
        val merchantDao = FakeMerchantDao()
        val useCase = newBackupDataUseCase(
            transactionDao = transactionDao,
            receiptDao = receiptDao,
            merchantDao = merchantDao,
        )
        useCase.importJson(testResource("backups/450-150.json").readText(Charsets.UTF_16))
        val transactionId = transactionDao.findAll().first().id
        val receipt = ReceiptEntity(
            transactionId = transactionId,
            sig = "DNX2500229500000595FB606FFCE12C6B8A38B439DE864D17261022185912.00",
            registerId = "DNX25002295",
            receiptNumber = 595,
            total = 12.0,
            issuedAt = Instant.parse("2026-10-22T15:59:00Z"),
            merchantAfm = "067022059",
            merchantName = "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ",
            netE = 12.0,
            vatE = 0.0,
            status = ReceiptStatus.FOUND,
            scannedAt = Instant.parse("2026-10-22T16:00:00Z"),
        )
        val merchant = MerchantEntity(
            afm = "067022059",
            displayName = "Barber",
            defaultCategoryId = null,
            defaultAccountId = null,
            updatedAt = Instant.parse("2026-10-22T16:00:00Z"),
        )
        receiptDao.save(receipt)
        merchantDao.save(merchant)

        // when
        val exportedJson = useCase.generateJsonBackup()
        val freshReceiptDao = FakeReceiptDao()
        val freshMerchantDao = FakeMerchantDao()
        newBackupDataUseCase(receiptDao = freshReceiptDao, merchantDao = freshMerchantDao)
            .importJson(exportedJson)

        // then
        freshReceiptDao.findAll() shouldBe listOf(receipt)
        freshMerchantDao.findAll() shouldBe listOf(merchant)
    }
}
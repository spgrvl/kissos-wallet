package com.spgrvl.kissos.data.backup

import com.spgrvl.kissos.base.TestDispatchersProvider
import com.spgrvl.kissos.base.di.KotlinxSerializationModule
import com.spgrvl.kissos.data.DataObserver
import com.spgrvl.kissos.data.db.dao.fake.FakeAccountDao
import com.spgrvl.kissos.data.db.dao.fake.FakeBankNotificationDao
import com.spgrvl.kissos.data.db.dao.fake.FakeBudgetDao
import com.spgrvl.kissos.data.db.dao.fake.FakeCategoryDao
import com.spgrvl.kissos.data.db.dao.fake.FakeLoanDao
import com.spgrvl.kissos.data.db.dao.fake.FakeLoanRecordDao
import com.spgrvl.kissos.data.db.dao.fake.FakeMerchantDao
import com.spgrvl.kissos.data.db.dao.fake.FakePlannedPaymentDao
import com.spgrvl.kissos.data.db.dao.fake.FakeReceiptDao
import com.spgrvl.kissos.data.db.dao.fake.FakeSettingsDao
import com.spgrvl.kissos.data.db.dao.fake.FakeTagAssociationDao
import com.spgrvl.kissos.data.db.dao.fake.FakeTagDao
import com.spgrvl.kissos.data.db.dao.fake.FakeTransactionDao
import com.spgrvl.kissos.data.db.entity.CategoryHintMemoryEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentEntity
import com.spgrvl.kissos.data.db.entity.DetectedPaymentStatus
import com.spgrvl.kissos.data.db.entity.MerchantEntity
import com.spgrvl.kissos.data.db.entity.PayeeMemoryEntity
import com.spgrvl.kissos.data.db.entity.ReceiptEntity
import com.spgrvl.kissos.data.model.ReceiptStatus
import com.spgrvl.kissos.data.repository.AccountRepository
import com.spgrvl.kissos.data.repository.CurrencyRepository
import com.spgrvl.kissos.data.repository.fake.fakeRepositoryMemoFactory
import com.spgrvl.kissos.data.repository.mapper.AccountMapper
import com.spgrvl.kissos.data.testResource
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.util.UUID

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
        bankNotificationDao: FakeBankNotificationDao = FakeBankNotificationDao(),
    ): BackupDataUseCase {
        val accountMapper = AccountMapper(
            CurrencyRepository(
                settingsDao = settingsDao,
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
            bankNotificationDao = bankNotificationDao,
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
            sig = "DZZ9900000100000595A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1261022185912.00",
            registerId = "DZZ99000001",
            receiptNumber = 595,
            total = 12.0,
            issuedAt = Instant.parse("2026-10-22T15:59:00Z"),
            merchantAfm = "999000101",
            merchantName = "ΔΟΚΙΜΗ ΚΟΥΡΕΙΟ",
            netE = 12.0,
            vatE = 0.0,
            status = ReceiptStatus.FOUND,
            scannedAt = Instant.parse("2026-10-22T16:00:00Z"),
        )
        val merchant = MerchantEntity(
            afm = "999000101",
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

    @Test
    fun `what was learned from bank notifications survives a backup, the payments don't`() = runTest {
        // given
        val bankNotificationDao = FakeBankNotificationDao()
        val useCase = newBackupDataUseCase(bankNotificationDao = bankNotificationDao)
        useCase.importJson(testResource("backups/450-150.json").readText(Charsets.UTF_16))
        val learnedAt = Instant.parse("2026-11-21T10:00:00Z")
        val payee = PayeeMemoryEntity(
            payee = "powerco",
            title = "Electricity",
            categoryId = null,
            updatedAt = learnedAt,
        )
        val hint = CategoryHintMemoryEntity(
            sourcePackage = "com.revolut.revolut",
            hint = "❤️",
            categoryId = UUID.randomUUID(),
            updatedAt = learnedAt,
        )
        bankNotificationDao.savePayee(payee)
        bankNotificationDao.saveCategoryHint(hint)
        bankNotificationDao.insertPayment(
            DetectedPaymentEntity(
                id = UUID.randomUUID(),
                dedupKey = "gr.payzy|0|1|46.65",
                sourcePackage = "gr.payzy",
                postedAt = learnedAt,
                amount = 46.65,
                currency = "EUR",
                direction = "OUT",
                payee = "PowerCo",
                categoryHint = null,
                status = DetectedPaymentStatus.NEW,
                transactionId = null,
                createdAt = learnedAt,
            )
        )

        // when
        val exportedJson = useCase.generateJsonBackup()
        val fresh = FakeBankNotificationDao()
        newBackupDataUseCase(bankNotificationDao = fresh).importJson(exportedJson)

        // then
        fresh.findAllPayees() shouldBe listOf(payee)
        fresh.findAllCategoryHints() shouldBe listOf(hint)
        fresh.observePayments(DetectedPaymentStatus.NEW).first() shouldBe emptyList()
        exportedJson shouldNotContain "46.65"
    }
}
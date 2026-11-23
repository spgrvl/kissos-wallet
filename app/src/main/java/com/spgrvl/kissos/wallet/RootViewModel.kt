package com.spgrvl.kissos.wallet

import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgrvl.kissos.banknotifications.BankNotificationIntents
import com.spgrvl.kissos.base.legacy.SharedPrefs
import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.base.legacy.stringRes
import com.spgrvl.kissos.base.model.TransactionType
import com.spgrvl.kissos.data.db.dao.read.SettingsDao
import com.spgrvl.kissos.data.receipt.ReceiptCode
import com.spgrvl.kissos.data.repository.LegalRepository
import com.spgrvl.kissos.domain.receipt.ReceiptLookupScheduler
import com.spgrvl.kissos.frp.test.TestIdlingResource
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.legacy.utils.ioThread
import com.spgrvl.kissos.legacy.utils.readOnly
import com.spgrvl.kissos.navigation.DisclaimerScreen
import com.spgrvl.kissos.navigation.EditTransactionScreen
import com.spgrvl.kissos.navigation.MainScreen
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.navigation.OnboardingScreen
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.domain.deprecated.logic.notification.TransactionReminderLogic
import com.spgrvl.kissos.wallet.migrations.MigrationsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
class RootViewModel @Inject constructor(
    private val kissosContext: KissosWalletCtx,
    private val nav: Navigation,
    private val settingsDao: SettingsDao,
    private val sharedPrefs: SharedPrefs,
    private val transactionReminderLogic: TransactionReminderLogic,
    private val receiptLookupScheduler: ReceiptLookupScheduler,
    private val migrationsManager: MigrationsManager,
    private val legalRepo: LegalRepository,
) : ViewModel() {

    companion object {
        const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"
        const val ACTION_SCAN_RECEIPT = "com.spgrvl.kissos.wallet.ACTION_SCAN_RECEIPT"

        const val USER_INACTIVITY_TIME_LIMIT = 60 // Time in seconds
    }

    private val _appLocked = MutableStateFlow<Boolean?>(null)
    val appLocked = _appLocked.readOnly()

    private var navigationStarted = false

    fun start(systemDarkMode: Boolean, intent: Intent) {
        viewModelScope.launch {
            TestIdlingResource.increment()

            ioThread {
                val theme = settingsDao.findAll().firstOrNull()?.theme
                    ?: if (systemDarkMode) Theme.DARK else Theme.LIGHT
                kissosContext.switchTheme(theme)

                kissosContext.initStartDayOfMonthInMemory(sharedPrefs = sharedPrefs)
            }

            TestIdlingResource.decrement()
        }

        // The activity is recreated when the theme or the app language changes; route the
        // user only on the first start so they stay on the screen they were on.
        if (navigationStarted) return
        navigationStarted = true

        viewModelScope.launch {
            TestIdlingResource.increment()

            ioThread {
                // initial app locked state
                _appLocked.value = isAppLockEnabled()

                if (isOnboardingCompleted()) {
                    navigateOnboardedUser(intent)
                } else {
                    nav.navigateTo(OnboardingScreen)
                }
                if (!legalRepo.isDisclaimerAccepted()) {
                    nav.navigateTo(DisclaimerScreen)
                }
            }

            TestIdlingResource.decrement()
        }

        viewModelScope.launch {
            migrationsManager.executeMigrations()
        }
    }

    /** A receipt link or widget action while the app is already open. */
    fun handleNewIntent(intent: Intent) {
        if (isOnboardingCompleted()) {
            handleSpecialStart(intent)
        }
    }

    private fun navigateOnboardedUser(intent: Intent) {
        nav.navigateTo(MainScreen)
        // A receipt link or widget action opens on top, so closing it leads home
        handleSpecialStart(intent)
        transactionReminderLogic.scheduleReminder()
        // Picks up receipts still waiting for AADE
        receiptLookupScheduler.schedule(restart = false)
    }

    private fun handleSpecialStart(intent: Intent) {
        (
            scannedReceiptScreen(intent)
                ?: scanReceiptScreen(intent)
                ?: detectedPaymentScreen(intent)
                ?: addTransactionScreen(intent)
            )?.let(nav::navigateTo)
    }

    /** The widgets' scan button: a new expense with the receipt scanner open. */
    private fun scanReceiptScreen(intent: Intent): EditTransactionScreen? =
        if (intent.action == ACTION_SCAN_RECEIPT) {
            EditTransactionScreen(
                initialTransactionId = null,
                type = TransactionType.EXPENSE,
                scanReceipt = true,
            )
        } else {
            null
        }

    /** Kissos's notification about a payment detected in a bank app's notification. */
    private fun detectedPaymentScreen(intent: Intent): EditTransactionScreen? =
        intent.getStringExtra(BankNotificationIntents.EXTRA_DETECTED_PAYMENT_ID)
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let {
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE,
                    detectedPaymentId = it,
                )
            }

    /** A receipt's link, opened from the phone's camera app or a QR scanner. */
    private fun scannedReceiptScreen(intent: Intent): EditTransactionScreen? =
        intent.dataString
            ?.takeIf { intent.action == Intent.ACTION_VIEW && ReceiptCode.parse(it) != null }
            ?.let {
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE,
                    receiptQrText = it,
                )
            }

    @Suppress("SwallowedException")
    private fun addTransactionScreen(intent: Intent): EditTransactionScreen? {
        val addTrnType: TransactionType? = try {
            intent.getSerializableExtra(EXTRA_ADD_TRANSACTION_TYPE) as? TransactionType
                ?: TransactionType.valueOf(intent.getStringExtra(EXTRA_ADD_TRANSACTION_TYPE) ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
        return addTrnType?.let {
            EditTransactionScreen(
                initialTransactionId = null,
                type = it
            )
        }
    }

    @Suppress("EmptyFunctionBlock")
    fun handleBiometricAuthResult(
        onAuthSuccess: () -> Unit = {}
    ): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Timber.d(stringRes(R.string.authentication_succeeded))
                unlockApp()
                onAuthSuccess()
            }

            override fun onAuthenticationFailed() {
                Timber.d(stringRes(R.string.authentication_failed))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            }
        }
    }

    private fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean(SharedPrefs.ONBOARDING_COMPLETED, false)
    }

    // App Lock & UserInactivity --------------------------------------------------------------------
    // Read each time (an in-memory lookup), so switching the lock in Settings applies right away
    fun isAppLockEnabled(): Boolean {
        return sharedPrefs.getBoolean(SharedPrefs.APP_LOCK_ENABLED, false)
    }

    fun isAppLocked(): Boolean {
        // by default we assume that the app is locked
        return appLocked.value ?: true
    }

    fun lockApp() {
        _appLocked.value = true
    }

    fun unlockApp() {
        _appLocked.value = false
        // Count the next absence from zero. Without this the counter stayed at the limit after
        // the first lock, so the app locked again the moment it was left, e.g. for the scanner.
        userInactiveJob?.cancel()
        resetUserInactiveTimer()
    }

    private val userInactiveTime = AtomicLong(0)
    private var userInactiveJob: Job? = null

    @Suppress("MagicNumber")
    fun startUserInactiveTimeCounter() {
        if (userInactiveJob != null && userInactiveJob!!.isActive) return

        userInactiveJob = viewModelScope.launch(Dispatchers.IO) {
            while (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT &&
                userInactiveJob != null && !userInactiveJob?.isCancelled!!
            ) {
                delay(1000)
                userInactiveTime.incrementAndGet()
            }

            if (isAppLockEnabled() && !isAppLocked()) {
                lockApp()
            }

            cancel()
        }
    }

    fun checkUserInactiveTimeStatus() {
        if (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT) {
            if (userInactiveJob != null && userInactiveJob?.isCancelled == false) {
                userInactiveJob?.cancel()
                resetUserInactiveTimer()
            }
        }
    }

    fun resetUserInactiveTimer() {
        userInactiveTime.set(0)
    }
}

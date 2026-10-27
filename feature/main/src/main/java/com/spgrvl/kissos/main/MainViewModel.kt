package com.spgrvl.kissos.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spgrvl.kissos.base.legacy.SharedPrefs
import com.spgrvl.kissos.data.repository.CurrencyRepository
import com.spgrvl.kissos.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.spgrvl.kissos.frp.test.TestIdlingResource
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.legacy.data.model.MainTab
import com.spgrvl.kissos.legacy.domain.deprecated.logic.AccountCreator
import com.spgrvl.kissos.legacy.utils.asLiveData
import com.spgrvl.kissos.legacy.utils.ioThread
import com.spgrvl.kissos.navigation.MainScreen
import com.spgrvl.kissos.navigation.Navigation
import com.spgrvl.kissos.wallet.domain.deprecated.logic.model.CreateAccountData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val kissosContext: KissosWalletCtx,
    private val nav: Navigation,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val accountCreator: AccountCreator,
    private val sharedPrefs: SharedPrefs,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val _currency = MutableLiveData<String>()
    val currency = _currency.asLiveData()

    fun start(screen: MainScreen) {
        nav.onBackPressed[screen] = {
            if (kissosContext.mainTab == MainTab.ACCOUNTS) {
                kissosContext.selectMainTab(MainTab.HOME)
                true
            } else {
                // Exiting (the backstack will close the app)
                false
            }
        }

        viewModelScope.launch {
            TestIdlingResource.increment()

            val baseCurrency = currencyRepository.getBaseCurrency()
            _currency.value = baseCurrency.code

            kissosContext.dataBackupCompleted =
                sharedPrefs.getBoolean(SharedPrefs.DATA_BACKUP_COMPLETED, false)

            ioThread {
                // Sync exchange rates
                syncExchangeRatesUseCase.sync(baseCurrency)
            }

            TestIdlingResource.decrement()
        }
    }

    fun selectTab(tab: MainTab) {
        kissosContext.selectMainTab(tab)
    }

    fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            TestIdlingResource.increment()

            accountCreator.createAccount(data) {}

            TestIdlingResource.decrement()
        }
    }
}

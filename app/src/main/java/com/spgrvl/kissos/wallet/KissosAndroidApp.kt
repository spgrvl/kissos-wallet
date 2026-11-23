package com.spgrvl.kissos.wallet

import com.spgrvl.kissos.ui.AppLanguage
import com.spgrvl.kissos.base.legacy.SharedPrefs
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.spgrvl.kissos.base.legacy.appContext
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

/**
 * Created by iliyan on 24.02.18.
 */
@HiltAndroidApp
class KissosAndroidApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appContext = this

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        AppLanguage.restore(this)
        suggestLanguageOnFirstLaunch()
    }

    /**
     * On a new install, starts in the suggested language (Greek for a Greek phone, SIM or
     * network) so even the first screen is in it; onboarding then lets the user change it.
     * Runs once, and never for users who already finished onboarding.
     */
    private fun suggestLanguageOnFirstLaunch() {
        if (sharedPrefs.getBoolean(SharedPrefs.ONBOARDING_COMPLETED, false)) return
        if (sharedPrefs.getBoolean(SharedPrefs.LANGUAGE_SUGGESTED, false)) return
        sharedPrefs.putBoolean(SharedPrefs.LANGUAGE_SUGGESTED, true)
        if (AppLanguage.current(this) == AppLanguage.SYSTEM) {
            AppLanguage.apply(AppLanguage.suggested(this), this)
        }
    }
}

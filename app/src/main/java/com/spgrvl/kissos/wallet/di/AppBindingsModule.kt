package com.spgrvl.kissos.wallet.di

import com.spgrvl.kissos.domain.AppStarter
import com.spgrvl.kissos.wallet.KissosAppStarter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun appStarter(appStarter: KissosAppStarter): AppStarter
}

package com.spgrvl.kissos.data.di

import com.spgrvl.kissos.data.remote.RemoteExchangeRatesDataSource
import com.spgrvl.kissos.data.remote.impl.RemoteExchangeRatesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    abstract fun bindExchangeRatesDataSource(
        datasource: RemoteExchangeRatesDataSourceImpl
    ): RemoteExchangeRatesDataSource
}

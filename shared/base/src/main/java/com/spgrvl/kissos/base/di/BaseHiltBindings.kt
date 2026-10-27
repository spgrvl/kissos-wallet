package com.spgrvl.kissos.base.di

import com.spgrvl.kissos.base.resource.AndroidResourceProvider
import com.spgrvl.kissos.base.resource.ResourceProvider
import com.spgrvl.kissos.base.threading.DispatchersProvider
import com.spgrvl.kissos.base.threading.IvyDispatchersProvider
import com.spgrvl.kissos.base.time.TimeConverter
import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.base.time.impl.DeviceTimeProvider
import com.spgrvl.kissos.base.time.impl.StandardTimeConverter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BaseHiltBindings {
    @Binds
    fun dispatchersProvider(impl: IvyDispatchersProvider): DispatchersProvider

    @Binds
    fun bindTimezoneProvider(impl: DeviceTimeProvider): TimeProvider

    @Binds
    fun bindTimeConverter(impl: StandardTimeConverter): TimeConverter

    @Binds
    fun resourceProvider(impl: AndroidResourceProvider): ResourceProvider
}
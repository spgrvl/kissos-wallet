package com.spgrvl.kissos.domain.di

import com.spgrvl.kissos.domain.features.Features
import com.spgrvl.kissos.domain.features.KissosFeatures
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface KissosCoreBindingsModule {
    @Binds
    fun bindFeatures(features: KissosFeatures): Features
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FeaturesEntryPoint {
    fun getFeatures(): Features
}

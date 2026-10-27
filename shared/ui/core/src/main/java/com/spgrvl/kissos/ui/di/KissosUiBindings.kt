package com.spgrvl.kissos.ui.di

import com.spgrvl.kissos.ui.time.DevicePreferences
import com.spgrvl.kissos.ui.time.TimeFormatter
import com.spgrvl.kissos.ui.time.impl.AndroidDateTimePicker
import com.spgrvl.kissos.ui.time.impl.AndroidDevicePreferences
import com.spgrvl.kissos.ui.time.impl.DateTimePicker
import com.spgrvl.kissos.ui.time.impl.KissosTimeFormatter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface KissosUiBindings {
    @Binds
    fun timeFormatter(impl: KissosTimeFormatter): TimeFormatter

    @Binds
    fun deviceTimePreferences(impl: AndroidDevicePreferences): DevicePreferences

    @Binds
    fun dateTimePicker(impl: AndroidDateTimePicker): DateTimePicker
}
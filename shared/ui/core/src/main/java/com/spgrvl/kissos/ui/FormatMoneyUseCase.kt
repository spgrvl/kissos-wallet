package com.spgrvl.kissos.ui

import android.content.Context
import com.spgrvl.kissos.domain.features.Features
import com.spgrvl.kissos.ui.time.DevicePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import javax.inject.Inject
import kotlin.math.abs

const val THOUSAND = 1_000
const val MILLION = 1_000_000
const val BILLION = 1_000_000_000

class FormatMoneyUseCase @Inject constructor(
    private val features: Features,
    private val devicePreferences: DevicePreferences,
    @ApplicationContext private val context: Context
) {

    suspend fun format(value: Double, shortenAmount: Boolean): String {
        // Read on every call: the app language can change while this instance is alive
        val symbols = DecimalFormatSymbols(devicePreferences.locale())
        val withoutDecimalFormatter = DecimalFormat("###,###", symbols)
        val withDecimalFormatter = DecimalFormat("###,###.00", symbols)
        val shortenAmountFormatter = DecimalFormat("###,###.##", symbols)

        if (abs(value) >= THOUSAND && shortenAmount) {
            val result = if (abs(value) >= BILLION) {
                "${shortenAmountFormatter.format(value / BILLION)}b"
            } else if (abs(value) >= MILLION) {
                "${shortenAmountFormatter.format(value / MILLION)}m"
            } else {
                "${shortenAmountFormatter.format(value / THOUSAND)}k"
            }
            return result
        } else {
            val showDecimalPoint = features.showDecimalNumber.isEnabled(context)

            val formatter = when (showDecimalPoint) {
                true -> withDecimalFormatter
                false -> withoutDecimalFormatter
            }
            return formatter.format(value)
        }
    }
}
package com.spgrvl.kissos.base

import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import com.spgrvl.kissos.base.legacy.appContext
import java.util.Currency
import java.util.Locale

/**
 * The currency suggested before the user picks one: the currency of the SIM's country,
 * else of the mobile network's country, else of the phone's region, else EUR. Reading the
 * SIM and network country needs no permission.
 *
 * The app language alone says nothing about the country (it can be just "el" or "en"),
 * which is why this doesn't use `Currency.getInstance(Locale.getDefault())`; that call also
 * throws for a locale without a region.
 */
object DefaultCurrency {
    const val FALLBACK = "EUR"

    fun code(context: Context? = contextOrNull()): String =
        countryCandidates(context).firstNotNullOfOrNull(::currencyOfCountry) ?: FALLBACK

    internal fun currencyOfCountry(country: String?): String? {
        if (country.isNullOrBlank() || country.length != 2) return null
        return runCatching {
            Currency.getInstance(Locale("", country.uppercase(Locale.ROOT)))?.currencyCode
        }.getOrNull()?.takeIf { it != "XXX" }
    }

    private fun countryCandidates(context: Context?): Sequence<String?> = sequence {
        val telephony = runCatching {
            context?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        }.getOrNull()
        yield(runCatching { telephony?.simCountryIso }.getOrNull())
        yield(runCatching { telephony?.networkCountryIso }.getOrNull())
        yield(runCatching { Resources.getSystem().configuration.locales[0].country }.getOrNull())
        yield(Locale.getDefault().country)
    }

    private fun contextOrNull(): Context? = runCatching { appContext }.getOrNull()
}

package com.spgrvl.kissos.ui

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.spgrvl.kissos.base.legacy.appContext
import java.util.Locale

/**
 * The app's own language. [SYSTEM] means no choice was made and the app follows the phone.
 *
 * On Android 13+ the choice lives in the system's per-app language setting (the same one
 * Settings → Apps → Kissos → Language changes), read and written directly. On older versions
 * it is kept in a private preference and handed to AppCompat on every start ([restore]).
 * AppCompat's own storage service isn't used: on its first run on Android 13+ it can wipe a
 * language already chosen in the system settings.
 */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    GREEK("el"),
    ENGLISH("en");

    companion object {
        private const val PREFS = "app_language"
        private const val KEY_TAG = "tag"

        /** The language the app is set to, or [SYSTEM] when it follows the phone. */
        fun current(context: Context = appContext): AppLanguage {
            val first = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Api33.get(context).takeIf { !it.isEmpty }?.get(0)?.language
            } else {
                storedTag(context)?.let(Locale::forLanguageTag)?.language
            }
            return fromLanguage(first)
        }

        /** Applies [language]; the visible screen is recreated in that language. */
        fun apply(language: AppLanguage, context: Context = appContext) {
            if (language == current(context)) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Api33.set(context, language.tag)
            } else {
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
                    if (language.tag == null) remove(KEY_TAG) else putString(KEY_TAG, language.tag)
                }
                AppCompatDelegate.setApplicationLocales(localeList(language.tag))
            }
        }

        /** Before Android 13: hands the saved choice to AppCompat. Call once per process start. */
        fun restore(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return
            storedTag(context)?.let { AppCompatDelegate.setApplicationLocales(localeList(it)) }
        }

        /**
         * The language to preselect on first launch: Greek when the phone is in Greek or
         * the SIM or network is Greek (a Greek with the phone in English still gets Greek),
         * English otherwise, since those are the app's only two languages.
         */
        fun suggested(context: Context): AppLanguage {
            val systemLanguage = runCatching {
                Resources.getSystem().configuration.locales[0].language
            }.getOrNull()
            val telephony = runCatching {
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            }.getOrNull()
            val countries = listOfNotNull(
                runCatching { telephony?.simCountryIso }.getOrNull(),
                runCatching { telephony?.networkCountryIso }.getOrNull(),
            )
            return suggested(systemLanguage, countries)
        }

        internal fun suggested(systemLanguage: String?, countries: List<String>): AppLanguage {
            val greek = systemLanguage.equals(GREEK.tag, ignoreCase = true) ||
                countries.any { it.equals("gr", ignoreCase = true) }
            return if (greek) GREEK else ENGLISH
        }

        private fun storedTag(context: Context): String? =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, null)

        private fun localeList(tag: String?): LocaleListCompat =
            tag?.let(LocaleListCompat::forLanguageTags) ?: LocaleListCompat.getEmptyLocaleList()

        private fun fromLanguage(language: String?): AppLanguage = when (language?.lowercase(Locale.ROOT)) {
            GREEK.tag -> GREEK
            ENGLISH.tag -> ENGLISH
            else -> SYSTEM
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private object Api33 {
        fun get(context: Context): LocaleList =
            context.getSystemService(LocaleManager::class.java)?.applicationLocales
                ?: LocaleList.getEmptyLocaleList()

        fun set(context: Context, tag: String?) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                tag?.let(LocaleList::forLanguageTags) ?: LocaleList.getEmptyLocaleList()
        }
    }
}

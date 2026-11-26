package com.spgrvl.kissos.legacy.utils

import com.spgrvl.kissos.base.DefaultCurrency
import android.app.KeyguardManager
import android.content.Context
import android.icu.util.Currency
import java.text.Normalizer
import java.util.Locale
import java.util.Random

fun <T> MutableList<T>.swap(fromIndex: Int, toIndex: Int) {
    val from = this[fromIndex]
    val to = this[toIndex]

    this[fromIndex] = to
    this[toIndex] = from
}

fun numberBetween(min: Double, max: Double): Double {
    return Random().nextDouble() * (max - min) + min
}

fun getDefaultFIATCurrency(): Currency = Currency.getInstance(DefaultCurrency.code())

fun String.toUpperCaseLocal() = uppercaseLocal()

fun String.toLowerCaseLocal() = this.lowercase(Locale.getDefault())

/**
 * Upper case for labels. Greek capitals are written without accents, so "Νοεμβρίου" becomes
 * "ΝΟΕΜΒΡΙΟΥ" (not "ΝΟΕΜΒΡΊΟΥ"); dialytika stay, as in "ΠΡΩΤΕΪΝΗ".
 */
fun String.uppercaseLocal(): String = uppercase(Locale.getDefault()).withoutGreekAccents()

private const val CombiningAcute = '\u0301'
private const val CombiningDiaeresis = '\u0308'
private val GreekLetters = '\u0370'..'\u03FF'

private fun String.withoutGreekAccents(): String {
    if (none { it in GreekLetters }) return this
    val decomposed = Normalizer.normalize(this, Normalizer.Form.NFD)
    val result = StringBuilder(decomposed.length)
    var afterGreekLetter = false
    for (char in decomposed) {
        when {
            char == CombiningAcute && afterGreekLetter -> Unit
            char == CombiningDiaeresis -> result.append(char)
            else -> {
                result.append(char)
                afterGreekLetter = char in GreekLetters
            }
        }
    }
    return Normalizer.normalize(result, Normalizer.Form.NFC)
}

fun String.capitalizeLocal(): String = this.replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(
            Locale.getDefault()
        )
    } else {
        it.toString()
    }
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.capitalizeLocal() }
}

fun hasLockScreen(context: Context): Boolean {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isDeviceSecure
}

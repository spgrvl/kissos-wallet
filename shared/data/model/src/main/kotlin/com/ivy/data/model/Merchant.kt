package com.ivy.data.model

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.ivy.data.model.exact.Exact
import com.ivy.data.model.primitive.NotBlankTrimmedString
import java.time.Instant

/** A shop, keyed by its tax id, with what to fill in when one of its receipts is scanned. */
data class Merchant(
    val afm: Afm,
    val displayName: NotBlankTrimmedString,
    val defaultCategory: CategoryId?,
    val defaultAccount: AccountId?,
    val updatedAt: Instant,
)

/** Greek tax id (ΑΦΜ): exactly 9 digits. */
@JvmInline
value class Afm private constructor(val value: String) {
    companion object : Exact<String, Afm> {
        private const val LENGTH = 9

        override val exactName = "Afm"

        override fun Raise<String>.spec(raw: String): Afm {
            val trimmed = raw.trim()
            ensure(trimmed.length == LENGTH && trimmed.all(Char::isDigit)) {
                "'$raw' is not $LENGTH digits"
            }
            return Afm(trimmed)
        }
    }
}

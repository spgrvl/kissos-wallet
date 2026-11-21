package com.spgrvl.kissos.data.receipt

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses a SimplyCloud document page. Its text, one line per HTML text node, reads e.g.:
 *
 * ```
 * ΔΟΚΙΜΑΣΤΙΚΟ ΚΡΕΟΠΩΛΕΙΟ ΟΕ
 * ΟΔΟΣ ΔΟΚΙΜΗΣ 1 ΑΘΗΝΑ GR
 * Αρ. ΓΕΜΗ:
 * ΑΦΜ:
 * 999000022
 * Στοιχεία παραστατικού
 * ΑΠΟΔΕΙΞΗ ΛΙΑΝΙΚΗΣ ΠΩΛΗΣΗΣ
 * Αριθμός
 * ΑΠΛ 100022
 * Ημερ.
 * 14/11/2026
 * …
 * Ανάλυση ΦΠΑ
 * ΦΠΑ 13%          (rate, net, VAT)
 * 16,19
 * 2,11
 * Λοιποί φόροι / Κρατήσεις
 * …
 * Σύνολο :
 * 18,30
 * MARK:
 * 400000000000022
 * ```
 */
object SimplyCloudPage {
    private val HIDDEN = Regex("(?is)<script\\b.*?</script>|<style\\b.*?</style>|<!--.*?-->")
    private val TAG = Regex("<[^>]+>")
    private val AFM = Regex("\\d{9}")
    private val SERIES_NUMBER = Regex("^(.*?)\\s*(\\d+)$")
    private val VAT_RATE = Regex("^ΦΠΑ (\\d+)\\s*%$")
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** The issuer's details come first, before these labels. */
    private val ISSUER_END = setOf("Αρ. ΓΕΜΗ:", "Τηλ.:", "ΑΦΜ:")

    fun parse(html: String): AadeLookup = parse(lines(html))

    fun lines(html: String): List<String> =
        html.replace(HIDDEN, " ").split(TAG).map(AadePage::text).filter { it.isNotEmpty() }

    fun parse(lines: List<String>): AadeLookup {
        fun after(label: String) = lines.indexOf(label).takeIf { it >= 0 }?.let { lines.getOrNull(it + 1) }

        val afm = after("ΑΦΜ:")?.takeIf(AFM::matches)
        val total = after("Σύνολο :")?.let(AadePage::money)
        val issuedOn = after("Ημερ.")?.let(::date)
        if (afm == null || total == null || issuedOn == null) {
            val missing = listOfNotNull(
                "AFM".takeIf { afm == null },
                "total".takeIf { total == null },
                "date".takeIf { issuedOn == null },
            )
            return AadeLookup.Unrecognized("SimplyCloud page missing: ${missing.joinToString()}")
        }
        val issuer = lines.takeWhile { it !in ISSUER_END }
        val number = after("Αριθμός")?.let(SERIES_NUMBER::find)?.groupValues
        val vat = vatByCategory(lines)
        return AadeLookup.Found(
            AadeReceipt(
                registerId = number?.get(1)?.ifEmpty { null },
                receiptNumber = number?.get(2)?.toLongOrNull(),
                mark = after("MARK:")?.toLongOrNull(),
                total = total,
                cardAmount = null,
                issuedAt = null,
                issuedOn = issuedOn,
                merchantAfm = afm,
                merchantName = issuer.getOrNull(0),
                address = issuer.getOrNull(1)?.removeSuffix(" GR"),
                documentType = after("Στοιχεία παραστατικού"),
                netByVatCategory = vat.mapValues { it.value.first },
                vatByVatCategory = vat.mapValues { it.value.second },
            )
        )
    }

    /** The VAT analysis is a line for the rate, then its net amount and VAT. */
    private fun vatByCategory(lines: List<String>): Map<Char, Pair<BigDecimal, BigDecimal>> {
        val section = lines.dropWhile { it != "Ανάλυση ΦΠΑ" }.takeWhile { !it.startsWith("Λοιποί φόροι") }
        return section.withIndex().mapNotNull { (i, line) ->
            val rate = VAT_RATE.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: return@mapNotNull null
            val category = AadePage.VAT_CATEGORY_BY_RATE[rate] ?: return@mapNotNull null
            val net = section.getOrNull(i + 1)?.let(AadePage::money) ?: return@mapNotNull null
            val vat = section.getOrNull(i + 2)?.let(AadePage::money) ?: return@mapNotNull null
            category to (net to vat)
        }.toMap()
    }

    private fun date(value: String): LocalDate? = try {
        LocalDate.parse(value, DATE_FORMAT)
    } catch (e: DateTimeParseException) {
        null
    }
}

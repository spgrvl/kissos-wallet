package com.spgrvl.kissos.data.receipt

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses an ILYDA document page (vs.gr). Its text, one line per HTML text node, reads e.g.:
 *
 * ```
 * ΔΟΚΙΜΑΣΤΙΚΟ ΦΑΡΜΑΚΕΙΟ ΙΚΕ          (the issuer's name, twice)
 * ΑΦΜ
 * EL999000066
 * …
 * Οδός
 * ΟΔΟΣ ΔΟΚΙΜΗΣ 6
 * ΑΘΗΝΑ
 * Τ.Κ.
 * …
 * Είδος Παραστατικού
 * Αριθμός
 * Ημερομηνία Έκδοσης
 * ΑΠΥ                                 (type, then series - number, then date)
 * ΑΠΥΜΒ
 * -
 * 1066
 * 14/11/2026 09:41
 * …
 * Ανάλυση Φ.Π.Α.
 * Φ.Π.Α. %
 * Καθαρό Ποσό ανά % Φ.Π.Α.
 * Ποσό Φ.Π.Α.
 * 24.00                               (rate, net, VAT)
 * 10.00
 * 2.40
 * Ανάλυση Συγκεντρωτικών Ποσών
 * …
 * ΠΛΗΡΩΤΕΟ ΠΟΣΟ ΠΕΛΑΤΗ
 * 12.40
 * …
 * ΜΑΡΚ
 * 400000000000066
 * ```
 */
object IlydaPage {
    private val AFM = Regex("^(?:EL)?(\\d{9})$")
    private val NUMBER = Regex("\\d+")
    private val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    /** The type, series and number come between the header and the date, in a few lines. */
    private const val DOCUMENT_LINES = 6
    private const val VAT_ROW = 3

    fun parse(html: String): AadeLookup = parse(SimplyCloudPage.lines(html))

    fun parse(lines: List<String>): AadeLookup {
        fun indexOf(label: String) = lines.indexOf(label).takeIf { it >= 0 }
        fun after(label: String) = indexOf(label)?.let { lines.getOrNull(it + 1) }

        val afm = after("ΑΦΜ")?.let { AFM.find(it)?.groupValues?.get(1) }
        val total = after("ΠΛΗΡΩΤΕΟ ΠΟΣΟ ΠΕΛΑΤΗ")?.let(AadePage::money)
        val document = document(lines)
        if (afm == null || total == null || document == null) {
            val missing = listOfNotNull(
                "AFM".takeIf { afm == null },
                "total".takeIf { total == null },
                "date".takeIf { document == null },
            )
            return AadeLookup.Unrecognized("ILYDA page missing: ${missing.joinToString()}")
        }
        val street = after("Οδός")
        val city = indexOf("Οδός")?.let { lines.getOrNull(it + 2) }?.takeIf { it != "Τ.Κ." }
        val vat = vatByCategory(lines)
        return AadeLookup.Found(
            AadeReceipt(
                registerId = document.series,
                receiptNumber = document.number,
                mark = after("ΜΑΡΚ")?.toLongOrNull(),
                total = total,
                cardAmount = null,
                issuedAt = document.issuedAt,
                issuedOn = document.issuedAt.toLocalDate(),
                merchantAfm = afm,
                merchantName = indexOf("ΑΦΜ")?.let { lines.getOrNull(it - 1) },
                address = listOfNotNull(street, city).joinToString(" ").ifEmpty { null },
                documentType = document.type,
                netByVatCategory = vat.mapValues { it.value.first },
                vatByVatCategory = vat.mapValues { it.value.second },
            )
        )
    }

    private class Document(val type: String?, val series: String?, val number: Long?, val issuedAt: LocalDateTime)

    /** E.g. "ΑΠΥ", "ΑΠΥΜΒ", "-", "1066", "14/11/2026 09:41" after the header. */
    private fun document(lines: List<String>): Document? {
        val values = lines.dropWhile { it != "Ημερομηνία Έκδοσης" }.drop(1).take(DOCUMENT_LINES)
        val (dateIndex, issuedAt) = values.withIndex()
            .firstNotNullOfOrNull { (i, line) -> dateTime(line)?.let { i to it } } ?: return null
        val fields = values.take(dateIndex).filter { it != "-" }
        val number = fields.lastOrNull()?.takeIf(NUMBER::matches)
        return Document(
            type = fields.firstOrNull()?.takeIf { it != number },
            series = fields.drop(1).dropLast(1).joinToString(" ").ifEmpty { null },
            number = number?.toLongOrNull(),
            issuedAt = issuedAt,
        )
    }

    /** The VAT analysis is the rate, net amount and VAT of each rate in turn. */
    private fun vatByCategory(lines: List<String>): Map<Char, Pair<BigDecimal, BigDecimal>> {
        val section = lines.dropWhile { it != "Ποσό Φ.Π.Α." }.drop(1)
            .takeWhile { it != "Ανάλυση Συγκεντρωτικών Ποσών" }
        return section.chunked(VAT_ROW).mapNotNull { row ->
            if (row.size < VAT_ROW) return@mapNotNull null
            val rate = AadePage.money(row[0])?.toInt() ?: return@mapNotNull null
            val category = AadePage.VAT_CATEGORY_BY_RATE[rate] ?: return@mapNotNull null
            val net = AadePage.money(row[1]) ?: return@mapNotNull null
            val vat = AadePage.money(row[2]) ?: return@mapNotNull null
            category to (net to vat)
        }.toMap()
    }

    private fun dateTime(value: String): LocalDateTime? = try {
        LocalDateTime.parse(value, DATE_TIME_FORMAT)
    } catch (e: DateTimeParseException) {
        null
    }
}

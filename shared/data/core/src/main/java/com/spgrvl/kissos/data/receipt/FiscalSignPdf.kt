package com.spgrvl.kissos.data.receipt

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses the PDF that a Fiscal Sign receipt's QR code downloads. It's the document as sent to myDATA,
 * with the shop's name, which AADE's own page leaves out, but no time. Its text reads e.g.:
 *
 * ```
 * SWEETS AND BREAD Ε Ε ΑΦΜ:  802231229
 * Tύπος: ΑΛΠ
 * Σειρά/Αριθμός: 15   127003 Ημ/νία Έκδοσης: 23/10/2026
 * 13,00 1,15 0,15 1,30            (VAT %, net, VAT, total)
 * Τελική Αξία: 1,30
 * M.ΑΡ.Κ:  400015798805805
 * ```
 */
object FiscalSignPdf {
    private val AFM = Regex("ΑΦΜ:\\s*(\\d{9})")
    private val TYPE = Regex("[TΤ]ύπος:\\s*(\\S+)")
    private val SERIES_NUMBER = Regex("Σειρά/Αριθμός:\\s*(\\S+)\\s+(\\d+)")
    private val DATE = Regex("Ημ/νία Έκδοσης:\\s*(\\d{2}/\\d{2}/\\d{4})")
    private val TOTAL = Regex("Τελική Αξία:\\s*([\\d.,]+)")
    private val MARK = Regex("[MΜ]\\.?ΑΡ\\.?Κ:\\s*(\\d+)")
    private val VAT_ROW = Regex("^(\\d+,\\d{2}) (\\d+,\\d{2}) (\\d+,\\d{2}) (\\d+,\\d{2})$")
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Groups of [VAT_ROW]. */
    private const val RATE = 1
    private const val NET = 2
    private const val VAT = 3

    /** Cash registers' VAT categories by rate, as [AadePage] reads them. */
    private val VAT_CATEGORY_BY_RATE = mapOf(6 to 'Α', 13 to 'Β', 24 to 'Γ', 36 to 'Δ', 0 to 'Ε')

    fun parse(pdf: ByteArray): AadeLookup = parse(PdfText.lines(pdf))

    fun parse(lines: List<String>): AadeLookup {
        val text = lines.joinToString("\n")
        fun find(regex: Regex, group: Int = 1) = regex.find(text)?.groupValues?.get(group)

        val afm = find(AFM)
        val total = find(TOTAL)?.let(AadePage::money)
        val issuedOn = find(DATE)?.let(::date)
        if (afm == null || total == null || issuedOn == null) {
            val missing = listOfNotNull(
                "AFM".takeIf { afm == null },
                "total".takeIf { total == null },
                "date".takeIf { issuedOn == null },
            )
            return AadeLookup.Unrecognized("Fiscal Sign receipt missing: ${missing.joinToString()}")
        }
        val vatRows = lines.mapNotNull { VAT_ROW.find(it.trim())?.groupValues }
            .mapNotNull { row ->
                val rate = AadePage.money(row[RATE])?.toInt() ?: return@mapNotNull null
                VAT_CATEGORY_BY_RATE[rate]?.let { it to row }
            }
        return AadeLookup.Found(
            AadeReceipt(
                registerId = find(SERIES_NUMBER),
                receiptNumber = find(SERIES_NUMBER, group = 2)?.toLongOrNull(),
                mark = find(MARK)?.toLongOrNull(),
                total = total,
                cardAmount = null,
                issuedAt = null,
                issuedOn = issuedOn,
                merchantAfm = afm,
                // The first line is the shop's name, then its ΑΦΜ
                merchantName = lines.firstOrNull()?.substringBefore("ΑΦΜ:")?.trim()?.ifEmpty { null },
                address = null,
                documentType = find(TYPE),
                netByVatCategory = vatRows.associate { (category, row) -> category to row.amount(NET) },
                vatByVatCategory = vatRows.associate { (category, row) -> category to row.amount(VAT) },
            )
        )
    }

    private fun List<String>.amount(index: Int): BigDecimal = AadePage.money(this[index]) ?: BigDecimal.ZERO

    private fun date(value: String): LocalDate? = try {
        LocalDate.parse(value, DATE_FORMAT)
    } catch (e: DateTimeParseException) {
        null
    }
}

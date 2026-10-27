package com.spgrvl.kissos.data.receipt

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses AADE's myDATA page for a document (`mydatapi.aade.gr/myDATA/TimologioQR/QRInfo?q=…`).
 * The details are read-only form inputs, e.g. `<input id="tamount" value="74,00">`.
 * It shows the date but no time, and leaves the issuer's name empty.
 */
object MyDataPage {
    private val INPUT = Regex("(?is)<input\\b[^>]*>")
    private val ID = Regex("(?i)\\bid=\"([^\"]*)\"")
    private val VALUE = Regex("(?i)\\bvalue=\"([^\"]*)\"")

    /** The ΜΑΡΚ cell is malformed HTML that ends with the number itself. */
    private val MARK = Regex("(?is)id=\"tMark\".*?(\\d{6,})\\s*</td>")
    private val CANCELLATION = Regex("(?is)<tr\\b[^>]*id=\"trcancellationMark\"[^>]*>(.*?)</tr>")
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun parse(html: String): AadeLookup {
        val inputs = INPUT.findAll(html).mapNotNull { input ->
            ID.find(input.value)?.groupValues?.get(1)?.let { id ->
                id to AadePage.text(VALUE.find(input.value)?.groupValues?.get(1).orEmpty())
            }
        }.toMap()
        val cancellation = CANCELLATION.find(html)?.groupValues?.get(1)?.let(AadePage::text)
        return if (cancellation.isNullOrBlank()) {
            found(html) { id -> inputs[id]?.ifBlank { null } }
        } else {
            AadeLookup.Rejected(cancellation)
        }
    }

    private fun found(html: String, input: (String) -> String?): AadeLookup {
        val total = input("tamount")?.let(AadePage::money)
        val issuedOn = input("tdate")?.let(::date)
        val afm = input("vatnumber")
        if (total == null || issuedOn == null || afm == null) {
            val missing = listOfNotNull(
                "total".takeIf { total == null },
                "date".takeIf { issuedOn == null },
                "AFM".takeIf { afm == null },
            )
            return AadeLookup.Unrecognized("myDATA page missing: ${missing.joinToString()}")
        }
        return AadeLookup.Found(
            AadeReceipt(
                registerId = input("snumber"),
                receiptNumber = input("saa")?.toLongOrNull(),
                mark = MARK.find(html)?.groupValues?.get(1)?.toLongOrNull(),
                total = total,
                cardAmount = null,
                issuedAt = null,
                issuedOn = issuedOn,
                merchantAfm = afm,
                merchantName = input("bname"),
                address = input("iaddress"),
                documentType = input("dtype"),
                netByVatCategory = emptyMap(),
                vatByVatCategory = emptyMap(),
            )
        )
    }

    private fun date(value: String): LocalDate? = try {
        LocalDate.parse(value.trim(), DATE_FORMAT)
    } catch (e: DateTimeParseException) {
        null
    }
}

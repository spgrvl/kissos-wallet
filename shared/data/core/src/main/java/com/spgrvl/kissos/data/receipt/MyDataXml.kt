package com.spgrvl.kissos.data.receipt

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Parses a document in myDATA's own XML format (`InvoicesDoc`), as invoiceportal.gr offers it.
 * It has the VAT breakdown and how it was paid, but only the issuer's ΑΦΜ, not their name, and no time:
 *
 * ```
 * <invoice>
 *   <mark>400000000000011</mark>
 *   <issuer><vatNumber>999000011</vatNumber>…</issuer>
 *   <invoiceHeader><series>0</series><aa>101011</aa><issueDate>2026-11-12</issueDate><invoiceType>11.1</invoiceType>…
 *   <paymentMethods><paymentMethodDetails><type>3</type><amount>11.40</amount></paymentMethodDetails></paymentMethods>
 *   <invoiceDetails>…<netValue>3.72</netValue><vatCategory>2</vatCategory><vatAmount>0.48</vatAmount>…</invoiceDetails>
 *   <invoiceSummary>…<totalGrossValue>11.40</totalGrossValue></invoiceSummary>
 * </invoice>
 * ```
 */
object MyDataXml {
    /** myDATA's payment method for POS and e-POS, i.e. by card. */
    private const val PAYMENT_BY_CARD = "7"

    /** myDATA's VAT categories that match a cash register's. The others are island rates or no VAT. */
    private val VAT_CATEGORY_BY_CODE = mapOf("1" to 'Γ', "2" to 'Β', "3" to 'Α', "7" to 'Ε')

    /** myDATA's document types, named as AADE's page for the document names them. */
    private val DOCUMENT_TYPES = mapOf(
        "1.1" to "Τιμολόγιο Πώλησης",
        "2.1" to "Τιμολόγιο Παροχής Υπηρεσιών",
        "8.6" to "Δελτίο Παραγγελίας Εστίασης",
        "11.1" to "ΑΛΠ (Απόδειξη Λιανικής Πώλησης)",
        "11.2" to "ΑΠΥ (Απόδειξη Παροχής Υπηρεσιών)",
        "11.3" to "Απλοποιημένο Τιμολόγιο",
        "11.4" to "Πιστωτικό Στοιχείο Λιανικής",
        "11.5" to "Απόδειξη Λιανικής Πώλησης για Λογ/σμό Τρίτων",
    )

    fun parse(xml: String): AadeLookup =
        xml.element("invoice")?.let(::found) ?: AadeLookup.Unrecognized("myDATA XML has no invoice")

    private fun found(invoice: String): AadeLookup {
        val header = invoice.element("invoiceHeader").orEmpty()
        val afm = invoice.element("issuer")?.value("vatNumber")
        val total = invoice.element("invoiceSummary")?.value("totalGrossValue")?.let(AadePage::money)
        val issuedOn = header.value("issueDate")?.let(::date)
        if (afm == null || total == null || issuedOn == null) {
            val missing = listOfNotNull(
                "AFM".takeIf { afm == null },
                "total".takeIf { total == null },
                "date".takeIf { issuedOn == null },
            )
            return AadeLookup.Unrecognized("myDATA XML missing: ${missing.joinToString()}")
        }
        val lines = invoice.elements("invoiceDetails")
            .mapNotNull { line -> VAT_CATEGORY_BY_CODE[line.value("vatCategory")]?.let { it to line } }
        val payments = invoice.element("paymentMethods")?.elements("paymentMethodDetails")
        return AadeLookup.Found(
            AadeReceipt(
                registerId = header.value("series"),
                receiptNumber = header.value("aa")?.toLongOrNull(),
                mark = invoice.value("mark")?.toLongOrNull(),
                total = total,
                cardAmount = payments?.filter { it.value("type") == PAYMENT_BY_CARD }?.sumOf { it.amount("amount") },
                issuedAt = null,
                issuedOn = issuedOn,
                merchantAfm = afm,
                merchantName = null,
                address = null,
                documentType = header.value("invoiceType")?.let(DOCUMENT_TYPES::get),
                netByVatCategory = lines.groupBy({ it.first }, { it.second.amount("netValue") })
                    .mapValues { (_, amounts) -> amounts.sumOf { it } },
                vatByVatCategory = lines.groupBy({ it.first }, { it.second.amount("vatAmount") })
                    .mapValues { (_, amounts) -> amounts.sumOf { it } },
            )
        )
    }

    /** Elements may have a namespace prefix, e.g. `<inv:invoice>`. */
    internal fun elementRegex(name: String): Regex = Regex("(?s)<(?:\\w+:)?$name\\b[^>]*>(.*?)</(?:\\w+:)?$name>")

    private fun String.element(name: String): String? = elementRegex(name).find(this)?.groupValues?.get(1)

    private fun String.elements(name: String): List<String> =
        elementRegex(name).findAll(this).map { it.groupValues[1] }.toList()

    private fun String.value(name: String): String? = element(name)?.let(AadePage::text)?.ifEmpty { null }

    private fun String.amount(name: String): BigDecimal = value(name)?.let(AadePage::money) ?: BigDecimal.ZERO

    private fun date(value: String): LocalDate? = try {
        LocalDate.parse(value)
    } catch (e: DateTimeParseException) {
        null
    }
}

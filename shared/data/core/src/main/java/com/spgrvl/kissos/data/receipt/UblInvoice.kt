package com.spgrvl.kissos.data.receipt

/**
 * Reads the seller of an invoice in UBL, the EU's standard format (EN 16931), as invoiceportal.gr offers it:
 *
 * ```
 * <cac:AccountingSupplierParty><cac:Party>…
 *   <cac:PartyTaxScheme><cbc:CompanyID>EL999000011</cbc:CompanyID>…</cac:PartyTaxScheme>
 *   <cac:PartyLegalEntity><cbc:RegistrationName>TEST GRILL Ε Ε</cbc:RegistrationName></cac:PartyLegalEntity>
 * ```
 */
object UblInvoice {
    private val SUPPLIER = MyDataXml.elementRegex("AccountingSupplierParty")
    private val COMPANY_ID = MyDataXml.elementRegex("CompanyID")
    private val REGISTRATION_NAME = MyDataXml.elementRegex("RegistrationName")

    /** Greek VAT numbers are the ΑΦΜ with the country prefix EL (or GR, as some software writes it). */
    private val GREEK_VAT_NUMBER = Regex("^(?:EL|GR)?(\\d{9})$", RegexOption.IGNORE_CASE)

    /** @return null unless the seller has both a Greek ΑΦΜ and a name. */
    fun supplier(xml: String): Issuer? {
        val supplier = SUPPLIER.find(xml)?.groupValues?.get(1) ?: return null
        val afm = COMPANY_ID.find(supplier)?.groupValues?.get(1)
            ?.let { GREEK_VAT_NUMBER.find(AadePage.text(it))?.groupValues?.get(1) }
        val name = REGISTRATION_NAME.find(supplier)?.groupValues?.get(1)?.let(AadePage::text)?.ifEmpty { null }
        return if (afm != null && name != null) Issuer(afm, name) else null
    }
}

package com.spgrvl.kissos.data.receipt

/** Who issued a document, as a provider's page or download names them. */
@Suppress("DataClassTypedIDs")
data class Issuer(val afm: String, val name: String)

/**
 * Fills in the issuer's name from another source than the lookup, e.g. the provider's page.
 * It only counts if it comes with the document's ΑΦΜ, so it can't be another party's, such as the customer's.
 */
fun AadeLookup.withIssuer(issuer: Issuer?): AadeLookup {
    val unnamed = (this as? AadeLookup.Found)?.receipt?.takeIf { it.merchantName == null } ?: return this
    return if (issuer != null && issuer.afm == unnamed.merchantAfm) {
        AadeLookup.Found(unnamed.copy(merchantName = issuer.name))
    } else {
        this
    }
}

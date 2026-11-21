package com.spgrvl.kissos.data.receipt

/**
 * Reads an Eskap page (`https://www.eskap.gr/invoice/<id>`) for its issuer and its link to the document on myDATA:
 *
 * ```
 * <div class="issuer-details"><ul class="list-unstyled">
 *   <li class="fw-semibold">ΔΟΚΙΜΑΣΤΙΚΗ ΠΙΤΣΑΡΙΑ Ε.Ε.</li>
 *   <li class="fw-light"> -  - </li>
 *   <li><span class="fw-normal">Α.Φ.Μ.: </span><span class="fw-light">999000033</span></li>…
 * ```
 */
object EskapPage {
    data class Document(val myData: ReceiptCode.MyData, val issuer: Issuer?)

    private val ISSUER_DETAILS = Regex("(?is)class=\"issuer-details\".*?</ul>")
    private val ITEM = Regex("(?is)<li\\b[^>]*>(.*?)</li>")
    private val AFM = Regex("Α\\.?Φ\\.?Μ\\.?:?\\s*(\\d{9})\\b")
    private val MYDATA_LINK = Regex("https://mydatapi\\.aade\\.gr/[^\"'<>\\s]+")

    /** @return null if the page doesn't link to myDATA. */
    fun parse(html: String): Document? {
        val myData = MYDATA_LINK.find(html)?.value?.replace("&amp;", "&")
            ?.let { ReceiptCode.parse(it) as? ReceiptCode.MyData }
            ?: return null
        return Document(myData, issuer(html))
    }

    private fun issuer(html: String): Issuer? {
        val details = ISSUER_DETAILS.find(html)?.value ?: return null
        val items = ITEM.findAll(details).map { AadePage.text(it.groupValues[1]) }.toList()
        val name = items.firstOrNull()?.takeUnless { it.isEmpty() || AFM.containsMatchIn(it) }
        val afm = items.firstNotNullOfOrNull { AFM.find(it)?.groupValues?.get(1) }
        return if (name != null && afm != null) Issuer(afm, name) else null
    }
}

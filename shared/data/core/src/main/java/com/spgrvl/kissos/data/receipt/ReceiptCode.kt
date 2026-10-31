package com.spgrvl.kissos.data.receipt

import java.net.URI
import java.net.URISyntaxException

/** A scanned receipt QR code, recognised offline. */
sealed interface ReceiptCode {
    /** A code whose details can be fetched directly, from AADE or the provider. */
    sealed interface Fetchable : ReceiptCode {
        /** Unique per receipt, stored as the receipt's `sig`. */
        val key: String
        val lookupUrl: String
    }

    /** A cash-register (ΦΗΜ) receipt. Its code holds the amount. */
    data class CashRegister(val qr: AadeQr) : Fetchable {
        override val key: String get() = qr.sig
        override val lookupUrl: String get() = qr.lookupUrl
    }

    /**
     * A document sent to myDATA, e.g. by a shop's invoicing software. Its code is a link to
     * AADE's page for it, `https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo?q=…`, and holds nothing else.
     */
    data class MyData(val url: String) : Fetchable {
        override val key: String get() = url
        override val lookupUrl: String get() = url
    }

    /**
     * A Fiscal Sign receipt. Its code downloads the document as a PDF, which has all the details,
     * e.g. `https://fiscalsign.gr/api/PrintInvoice/download/<id>`.
     */
    data class FiscalSign(val url: String) : Fetchable {
        override val key: String get() = url
        override val lookupUrl: String get() = url
    }

    /**
     * A page of an e-invoicing provider, which links to the document's [MyData] page.
     * Most providers build these pages in the browser behind bot checks, so only a WebView can open them.
     */
    data class Provider(val url: String) : ReceiptCode

    companion object {
        private const val MYDATA_PATH = "/TimologioQR/QRInfo"
        private const val FISCAL_SIGN_PATH = "/api/PrintInvoice/download/"
        private val ESCAPE = Regex("%[0-9a-fA-F]{2}")

        /** Hosts of the e-invoicing providers whose pages are known to link to myDATA. */
        private val PROVIDER_HOSTS = listOf(
            // Epsilon Digital, e.g. https://epsilondigital6.epsilonnet.gr/fd/<id>
            "epsilonnet.gr",
            // EntersoftOne, e.g. https://e-invoicing.gr/edocuments/ViewInvoice/-1/<id>
            "e-invoicing.gr",
        )

        /** @return null for anything that isn't a supported receipt code. */
        fun parse(qrText: String): ReceiptCode? {
            val text = qrText.trim()
            return AadeQr.parse(text)?.let(::CashRegister) ?: parseUrl(text)
        }

        /** Recognises a stored receipt's [Fetchable.key]. */
        fun fromKey(key: String): Fetchable? = parse(key) as? Fetchable

        @Suppress("ReturnCount")
        private fun parseUrl(text: String): ReceiptCode? {
            if (!text.startsWith("http", ignoreCase = true)) return null
            val uri = try {
                URI(text)
            } catch (e: URISyntaxException) {
                return null
            }
            val host = uri.host?.lowercase() ?: return null
            val path = uri.rawPath.orEmpty()
            return when {
                host.isDomain("aade.gr") && path.endsWith(MYDATA_PATH, ignoreCase = true) &&
                    uri.rawQuery?.contains("q=") == true ->
                    MyData("https://${uri.rawAuthority}$path?${uri.rawQuery.upperCaseEscapes()}")

                host.isDomain("fiscalsign.gr") && path.startsWith(FISCAL_SIGN_PATH, ignoreCase = true) ->
                    FiscalSign(text)

                PROVIDER_HOSTS.any { host.isDomain(it) } -> Provider(text)
                else -> null
            }
        }

        private fun String.isDomain(domain: String): Boolean = this == domain || endsWith(".$domain")

        /** Providers' pages link with `%2f`, printed codes with `%2F`; both are the same document. */
        private fun String.upperCaseEscapes(): String = replace(ESCAPE) { it.value.uppercase() }
    }
}

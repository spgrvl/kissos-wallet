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
     * An invoiceportal.gr (BusinessCloud) page, e.g. `https://invoiceportal.gr/invoices/2026-<uid>`.
     * It offers the document as sent to myDATA, as XML at `<page>/myDATA`, which leaves out the issuer's name,
     * and as UBL (EN 16931) at [ublUrl], which has it.
     */
    data class InvoicePortal(val url: String) : Fetchable {
        override val key: String get() = url
        override val lookupUrl: String get() = "$url/myDATA"
        val ublUrl: String get() = "$url/EN16931"
    }

    /**
     * An Eskap page, e.g. `https://www.eskap.gr/invoice/<id>`. It names the issuer and links to the
     * document's [MyData] page, which has the rest. Unlike most providers' pages, plain HTTP can read it.
     */
    data class Eskap(val url: String) : Fetchable {
        override val key: String get() = url
        override val lookupUrl: String get() = url
    }

    /**
     * A SimplyCloud page, e.g. `https://app.simplycloud.gr/InvoiceData/v/GR<ΑΦΜ>-<id>`,
     * which shows the whole document but doesn't link to myDATA.
     */
    data class SimplyCloud(val url: String) : Fetchable {
        override val key: String get() = url
        override val lookupUrl: String get() = url
    }

    /**
     * An ILYDA page, e.g. `https://vs.gr/iv/invoice/download/<tenant>/<tenant>~k1~<id>~<token>`,
     * which shows the whole document but doesn't link to myDATA.
     */
    data class Ilyda(val url: String) : Fetchable {
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
        private val HTTP_SCHEME = Regex("^http:", RegexOption.IGNORE_CASE)
        private val INVOICE_PORTAL_PATH = Regex("^/invoices/([^/]+)/?$", RegexOption.IGNORE_CASE)
        private const val SIMPLY_CLOUD_PATH = "/InvoiceData/v/"
        private const val ESKAP_PATH = "/invoice/"
        private const val ILYDA_PATH = "/iv/invoice/download/"

        /** Hosts, and where their documents are, of the e-invoicing providers whose pages link to myDATA. */
        private val PROVIDER_PAGES = listOf(
            // Epsilon Digital, e.g. https://epsilondigital6.epsilonnet.gr/fd/<id>
            "epsilonnet.gr" to "/",
            // EntersoftOne, e.g. https://e-invoicing.gr/edocuments/ViewInvoice/-1/<id>
            "e-invoicing.gr" to "/",
            // Impact, e.g. https://einvoice.impact.gr/p/EL<ΑΦΜ>/<id>/<id>
            // or https://einvoice.impact.gr/v/EL<ΑΦΜ>-<number>-<id>-<id>
            "einvoice.impact.gr" to "/p/",
            "einvoice.impact.gr" to "/v/",
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
            // Printed codes can say http, but the app only makes secure connections
            val secureUrl = text.replace(HTTP_SCHEME, "https:")
            return fetchable(uri, host, path, secureUrl)
                ?: Provider(secureUrl).takeIf {
                    PROVIDER_PAGES.any { (domain, pages) -> host.isDomain(domain) && path.startsWith(pages) }
                }
        }

        /** The documents Kissos downloads itself, without a WebView. */
        private fun fetchable(uri: URI, host: String, path: String, secureUrl: String): Fetchable? =
            when {
                host.isDomain("aade.gr") && path.endsWith(MYDATA_PATH, ignoreCase = true) &&
                    uri.rawQuery?.contains("q=") == true ->
                    MyData("https://${uri.rawAuthority}$path?${uri.rawQuery.upperCaseEscapes()}")

                host.isDomain("fiscalsign.gr") && path.startsWith(FISCAL_SIGN_PATH, ignoreCase = true) ->
                    FiscalSign(secureUrl)

                host.isDomain("invoiceportal.gr") && INVOICE_PORTAL_PATH.matches(path) ->
                    InvoicePortal("https://${uri.rawAuthority}${path.removeSuffix("/")}")

                host.isDomain("simplycloud.gr") && path.startsWith(SIMPLY_CLOUD_PATH, ignoreCase = true) ->
                    SimplyCloud(secureUrl)

                host.isDomain("eskap.gr") && path.startsWith(ESKAP_PATH) && path.length > ESKAP_PATH.length ->
                    Eskap(secureUrl)

                host.isDomain("vs.gr") && path.startsWith(ILYDA_PATH) && path.length > ILYDA_PATH.length ->
                    Ilyda(secureUrl)

                else -> null
            }

        private fun String.isDomain(domain: String): Boolean = this == domain || endsWith(".$domain")

        /** Providers' pages link with `%2f`, printed codes with `%2F`; both are the same document. */
        private fun String.upperCaseEscapes(): String = replace(ESCAPE) { it.value.uppercase() }
    }
}

package com.ivy.data.receipt

import java.math.BigDecimal
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Contents of a Greek cash-register (ΦΗΜ) receipt QR code, parsed fully offline.
 *
 * The QR holds `https://www1.aade.gr/tameiakes/myweb/q1.php?SIG=<sig>`. AADE's own
 * manual-entry form on that page builds SIG from four fields:
 *
 *  - 11 chars   register id (ΦΗΜ / ΑΜΗΤΜ printed on the receipt), e.g. `DNX25002295`
 *  -  8 digits  progressive receipt number, zero-padded
 *  - 40 chars   signature field
 *  - the rest   total amount, e.g. `12.00`
 *
 * So the amount is available without any network call.
 */
@Suppress("DataClassTypedIDs")
data class AadeQr(
    val sig: String,
    val registerId: String,
    val receiptNumber: Long,
    val signatureField: String,
    val total: BigDecimal,
    /**
     * Most registers end the 40-char signature field with the issue time as YYMMDDHHMM.
     * Some don't (e.g. the supermarket's `DLD…` register puts a full 40-char hash there),
     * so this is null for them. Treat it as a hint: AADE's page is authoritative.
     */
    val issuedAtHint: LocalDateTime?,
) {
    val lookupUrl: String get() = "$LOOKUP_URL?SIG=$sig"

    companion object {
        const val LOOKUP_URL = "https://www1.aade.gr/tameiakes/myweb/q1.php"
        private const val LOOKUP_PATH = "/tameiakes/myweb/q1.php"

        private const val REGISTER_LEN = 11
        private const val NUMBER_LEN = 8
        private const val SIGNATURE_LEN = 40
        private const val PREFIX_LEN = REGISTER_LEN + NUMBER_LEN + SIGNATURE_LEN
        private const val HINT_LEN = 10

        private val REGISTER = Regex("[A-Z0-9]{11}")
        private val DIGITS = Regex("\\d+")
        private val AMOUNT = Regex("\\d+(?:[.,]\\d{1,2})?")
        private val HINT_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("uuMMddHHmm").withResolverStyle(ResolverStyle.STRICT)

        /**
         * Accepts the scanned QR text (the full URL) or a bare SIG value.
         * Returns null for anything that isn't an AADE cash-register receipt code:
         * e-invoicing provider links, IRIS payment codes, random QR codes.
         */
        fun parse(qrText: String): AadeQr? {
            val text = qrText.trim()
            val sig = if (text.startsWith("http", ignoreCase = true)) sigFromUrl(text) else text
            return sig?.let(::parseSig)
        }

        @Suppress("ReturnCount")
        private fun sigFromUrl(url: String): String? {
            val uri = try {
                URI(url)
            } catch (e: URISyntaxException) {
                return null
            }
            val host = uri.host?.lowercase() ?: return null
            // Current receipts use www1.aade.gr; older ones may still point at www1.gsis.gr.
            if (!(host.endsWith("aade.gr") || host.endsWith("gsis.gr"))) return null
            if (uri.path?.endsWith(LOOKUP_PATH) != true) return null
            return uri.rawQuery?.split('&')
                ?.map { it.split('=', limit = 2) }
                ?.firstOrNull { it.size == 2 && it[0].equals("SIG", ignoreCase = true) }
                ?.let { URLDecoder.decode(it[1], Charsets.UTF_8.name()) }
        }

        @Suppress("ReturnCount")
        private fun parseSig(raw: String): AadeQr? {
            val sig = raw.trim()
            if (sig.length <= PREFIX_LEN) return null
            val register = sig.substring(0, REGISTER_LEN)
            val number = sig.substring(REGISTER_LEN, REGISTER_LEN + NUMBER_LEN)
            val signature = sig.substring(REGISTER_LEN + NUMBER_LEN, PREFIX_LEN)
            val amount = sig.substring(PREFIX_LEN)
            if (!REGISTER.matches(register) || !DIGITS.matches(number) || !AMOUNT.matches(amount)) {
                return null
            }
            return AadeQr(
                sig = sig,
                registerId = register,
                receiptNumber = number.toLong(),
                signatureField = signature,
                total = BigDecimal(amount.replace(',', '.')),
                issuedAtHint = timeHint(signature),
            )
        }

        private fun timeHint(signature: String): LocalDateTime? {
            val tail = signature.takeLast(HINT_LEN)
            if (!DIGITS.matches(tail)) return null
            return try {
                LocalDateTime.parse(tail, HINT_FORMAT)
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }
}

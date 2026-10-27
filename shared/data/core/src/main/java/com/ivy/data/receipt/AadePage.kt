package com.ivy.data.receipt

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Outcome of looking a receipt up on AADE's page for it (the URL in the QR). */
sealed interface AadeLookup {
    /** The receipt has reached AADE; full details are known. */
    data class Found(val receipt: AadeReceipt) : AadeLookup

    /**
     * The register is real and declared, but this receipt hasn't reached AADE yet.
     * Registers upload periodically (fuel stations only at end of day), so this is
     * normal right after paying. The merchant is already known; retry later for the rest.
     */
    @Suppress("DataClassTypedIDs")
    data class NotYetTransmitted(
        val registerId: String?,
        val merchantAfm: String?,
        val merchantName: String?,
        val address: String?,
    ) : AadeLookup

    /** AADE rejected the code: unreadable, or the register is inactive or undeclared. */
    data class Rejected(val message: String) : AadeLookup

    /** The page matched no known layout. AADE probably redesigned it; the parser needs updating. */
    data class Unrecognized(val reason: String) : AadeLookup
}

@Suppress("DataClassTypedIDs")
data class AadeReceipt(
    /** The cash register, or the series of a myDATA document. */
    val registerId: String?,
    val receiptNumber: Long?,
    /** myDATA's unique number (ΜΑΡΚ). Null for cash-register receipts. */
    val mark: Long?,
    val total: BigDecimal,
    /**
     * What the register reported as paid by card, null for myDATA documents. Not fully reliable:
     * a supermarket receipt paid by card ("EFT" on the paper) reports 0.00.
     */
    val cardAmount: BigDecimal?,
    /** Null when AADE only shows the date, as for myDATA documents. */
    val issuedAt: LocalDateTime?,
    val issuedOn: LocalDate,
    val merchantAfm: String,
    /**
     * Legal name, truncated by AADE (e.g. "ΠΑΝΑΓΟΠΟΥΛΟΣ ΧΡΗΣΤ"), not the shop's trading name.
     * myDATA pages leave it empty.
     */
    val merchantName: String?,
    val address: String?,
    val documentType: String?,
    /** Keyed by VAT category letter. Observed: Α = 6%, Β = 13%, Γ = 24%, Ε = 0%. */
    val netByVatCategory: Map<Char, BigDecimal>,
    val vatByVatCategory: Map<Char, BigDecimal>,
) {
    enum class Payment { CARD, NOT_CARD, SPLIT }

    val payment: Payment?
        get() = when {
            cardAmount == null -> null
            cardAmount.signum() == 0 -> Payment.NOT_CARD
            cardAmount >= total -> Payment.CARD
            else -> Payment.SPLIT
        }
}

object AadePage {
    val VAT_CATEGORIES = listOf('Α', 'Β', 'Γ', 'Δ', 'Ε')

    private val SCRIPT = Regex("(?is)<script\\b.*?</script>")
    private val COMMENT = Regex("(?s)<!--.*?-->")
    private val TAG = Regex("<[^>]+>")
    private val SPACES = Regex("[\\s\\u00A0\\uFEFF]+")
    private val ROW = Regex("(?is)<tr\\b[^>]*>(.*?)</tr>")
    private val CELL = Regex("(?is)<t[dh]\\b[^>]*>(.*?)</t[dh]>")
    private val CALLOUT = Regex("(?is)<div\\s+class=\"callout\\s+([a-z]+)[^\"]*\"[^>]*>(.*?)</div>")
    private val NUMERIC_ENTITY = Regex("&#(x?)([0-9a-fA-F]+);")
    private val AFM = Regex("ΑΦΜ\\s*(\\d{9})")
    private val DATE_FORMATS = listOf("yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss")
        .map(DateTimeFormatter::ofPattern)
    private const val DECIMAL_RADIX = 10
    private const val HEX_RADIX = 16

    fun parse(html: String): AadeLookup {
        val page = html.replace(SCRIPT, " ").replace(COMMENT, " ")
        val callouts = CALLOUT.findAll(page)
            .map { it.groupValues[1].lowercase() to text(it.groupValues[2]) }
            .toList()
        val rows = ROW.findAll(page).mapNotNull { row ->
            val cells = CELL.findAll(row.groupValues[1]).map { text(it.groupValues[1]) }.toList()
            if (cells.size >= 2) cells[0] to cells[1] else null
        }.toMap()

        fun row(label: String) = rows[label]?.ifBlank { null }
        fun rowStartingWith(prefix: String) =
            rows.entries.firstOrNull { it.key.startsWith(prefix) }?.value?.ifBlank { null }

        // Check errors first: for an unknown register AADE shows the error box AND a
        // contradictory "register is active" warning box on the same page.
        val alerts = callouts.filter { it.first == "alert" }
        val warning = callouts.firstOrNull { it.first == "warning" }
        return when {
            alerts.isNotEmpty() -> AadeLookup.Rejected(alerts.joinToString(" ") { it.second })
            callouts.any { it.first == "success" } -> found(::row)
            warning != null -> AadeLookup.NotYetTransmitted(
                registerId = row("ΦΗΜ"),
                merchantAfm = AFM.find(warning.second)?.groupValues?.get(1),
                merchantName = row("Επωνυμία"),
                address = rowStartingWith("Διεύθυνση"),
            )

            else -> AadeLookup.Unrecognized("no status box found")
        }
    }

    @Suppress("ComplexCondition")
    private fun found(row: (String) -> String?): AadeLookup {
        val total = row("Συνολική αξία")?.let(::money)
        val card = row("Ποσό πληρωμής με κάρτα")?.let(::money)
        val issuedAt = row("Ημερομηνία, ώρα")?.let(::dateTime)
        val register = row("ΦΗΜ")
        val afm = row("ΑΦΜ εκδότη")
        val name = row("Επωνυμία")
        if (total == null || card == null || issuedAt == null || register == null || afm == null || name == null) {
            val missing = listOfNotNull(
                "total".takeIf { total == null },
                "card amount".takeIf { card == null },
                "date".takeIf { issuedAt == null },
                "register".takeIf { register == null },
                "AFM".takeIf { afm == null },
                "name".takeIf { name == null },
            )
            return AadeLookup.Unrecognized("receipt page missing: ${missing.joinToString()}")
        }
        return AadeLookup.Found(
            AadeReceipt(
                registerId = register,
                receiptNumber = row("Προοδευτικός α/α")?.toLongOrNull(),
                mark = null,
                total = total,
                cardAmount = card,
                issuedAt = issuedAt,
                issuedOn = issuedAt.toLocalDate(),
                merchantAfm = afm,
                merchantName = name,
                address = row("Διεύθυνση"),
                documentType = row("Είδος παραστατικού"),
                netByVatCategory = byVatCategory("Καθαρή αξία", row),
                vatByVatCategory = byVatCategory("ΦΠΑ", row),
            )
        )
    }

    private fun byVatCategory(prefix: String, row: (String) -> String?): Map<Char, BigDecimal> =
        VAT_CATEGORIES.mapNotNull { letter ->
            row("$prefix $letter")?.let(::money)?.let { letter to it }
        }.toMap()

    internal fun text(fragment: String): String =
        decodeEntities(fragment.replace(TAG, " ")).replace(SPACES, " ").trim()

    private fun decodeEntities(s: String): String = s
        .replace(NUMERIC_ENTITY) { m ->
            val radix = if (m.groupValues[1].isEmpty()) DECIMAL_RADIX else HEX_RADIX
            m.groupValues[2].toIntOrNull(radix)?.let { String(Character.toChars(it)) } ?: m.value
        }
        .replace("&euro;", "€").replace("&nbsp;", " ").replace("&quot;", "\"")
        .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")

    /** "€ 12.00" → 12.00. Also copes with "1,234.56" and "12,00" in case AADE ever uses them. */
    internal fun money(value: String): BigDecimal? {
        var s = value.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
        if (s.isEmpty()) return null
        if ('.' in s && ',' in s) {
            s = if (s.lastIndexOf('.') > s.lastIndexOf(',')) s.replace(",", "") else s.replace(".", "")
        }
        return s.replace(',', '.').toBigDecimalOrNull()
    }

    private fun dateTime(value: String): LocalDateTime? = DATE_FORMATS.firstNotNullOfOrNull {
        try {
            LocalDateTime.parse(value.trim(), it)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

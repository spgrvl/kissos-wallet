package com.ivy.data.receipt

import com.ivy.data.testResource
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Four real receipts. Every expected value was read off the paper receipts,
 * not copied from parser output.
 */
@Suppress("LongParameterList")
enum class AadeTestReceipt(
    val qr: String,
    val registerId: String,
    val receiptNumber: Long,
    val total: String,
    val timeHint: String?,
    val fixture: String,
    val afm: String,
    val merchantName: String,
    val issuedAt: String,
    val cardAmount: String,
    val payment: AadeReceipt.Payment,
    val net: Map<Char, String>,
    val vat: Map<Char, String>,
) {
    BARBER(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DZZ9900000100000595A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1261022185912.00",
        registerId = "DZZ99000001",
        receiptNumber = 595,
        total = "12.00",
        timeHint = "2026-10-22T18:59",
        fixture = "barber",
        afm = "999000101",
        merchantName = "ΔΟΚΙΜΗ ΚΟΥΡΕΙΟ",
        issuedAt = "2026-10-22T18:59",
        cardAmount = "0.00",
        payment = AadeReceipt.Payment.NOT_CARD,
        net = mapOf('Ε' to "12.00"),
        vat = emptyMap(),
    ),

    // No time hint in the QR. Paid by card ("EFT 11,87" on paper), but the register reports 0.00.
    SUPERMARKET(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DZZ9900000200288768D4D4D4D4D4D4D4D4D4D4D4D4D4D4D4D4D4D4D4D411.87",
        registerId = "DZZ99000002",
        receiptNumber = 288768,
        total = "11.87",
        timeHint = null,
        fixture = "supermarket",
        afm = "999000102",
        merchantName = "ΔΟΚΙΜΗ ΣΟΥΠΕΡ ΜΑΡΚΕΤ ΑΕ",
        issuedAt = "2026-10-22T11:18",
        cardAmount = "0.00",
        payment = AadeReceipt.Payment.NOT_CARD,
        net = mapOf('Β' to "10.50"),
        vat = mapOf('Β' to "1.37"),
    ),
    PHARMACY(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DZZ9900000300028558C3C3C3C3C3C3C3C3C3C3C3C3C3C3C326102014215.09",
        registerId = "DZZ99000003",
        receiptNumber = 28558,
        total = "5.09",
        timeHint = "2026-10-20T14:21",
        fixture = "pharmacy",
        afm = "999000103",
        merchantName = "ΔΟΚΙΜΗ ΦΑΡΜΑΚ",
        issuedAt = "2026-10-20T14:21",
        cardAmount = "5.09",
        payment = AadeReceipt.Payment.CARD,
        net = mapOf('Α' to "4.80"),
        vat = mapOf('Α' to "0.29"),
    ),

    // Two VAT rates.
    GRILL(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DZZ9900000400025460B2B2B2B2B2B2B2B2B2B2B2B2B2B2B2261017205812.80",
        registerId = "DZZ99000004",
        receiptNumber = 25460,
        total = "12.80",
        timeHint = "2026-10-17T20:58",
        fixture = "grill",
        afm = "999000104",
        merchantName = "ΔΟΚΙΜΗ ΨΗΣΤΑΡ",
        issuedAt = "2026-10-17T20:58",
        cardAmount = "12.80",
        payment = AadeReceipt.Payment.CARD,
        net = mapOf('Β' to "9.56", 'Γ' to "1.61"),
        vat = mapOf('Β' to "1.24", 'Γ' to "0.39"),
    ),
}

fun money(value: String): BigDecimal = BigDecimal(value)

fun at(value: String): LocalDateTime = LocalDateTime.parse(value)

/** A real myDATA link, from an Epsilon Digital fuel receipt. Its page is `mydata/shell.html`. */
const val ShellMyDataUrl = "https://mydatapi.aade.gr/myDATA/TimologioQR/QRInfo" +
    "?q=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
    "%2FBBBBB%2FCCCCC%2FDDDDDDDDDDDDDDDD%3D"

fun myDataPage(name: String): String = testResource("mydata/$name.html").readText()

/** A real AADE page: the shared head and tail around one `body_*.html`. */
fun aadePage(body: String, crlf: Boolean = false): String {
    val html = listOf("head.html", "body_$body.html", "tail.html")
        .joinToString("") { testResource("aade/$it").readText().replace("\r\n", "\n") }
    return if (crlf) html.replace("\n", "\r\n") else html
}

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
            "?SIG=DNX2500229500000595FB606FFCE12C6B8A38B439DE864D17261022185912.00",
        registerId = "DNX25002295",
        receiptNumber = 595,
        total = "12.00",
        timeHint = "2026-10-22T18:59",
        fixture = "barber",
        afm = "067022059",
        merchantName = "ΚΟΥΜΑΝΤΟΣ ΓΕΩΡΓ",
        issuedAt = "2026-10-22T18:59",
        cardAmount = "0.00",
        payment = AadeReceipt.Payment.NOT_CARD,
        net = mapOf('Ε' to "12.00"),
        vat = emptyMap(),
    ),

    // No time hint in the QR. Paid by card ("EFT 11,87" on paper), but the register reports 0.00.
    SUPERMARKET(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DLD23030475002887683227C266EC666B8EA418BF47289F363AAAC5C14F11.87",
        registerId = "DLD23030475",
        receiptNumber = 288768,
        total = "11.87",
        timeHint = null,
        fixture = "supermarket",
        afm = "094185561",
        merchantName = "ΑΝΔΡΙΚΟΠΟΥΛΟΣ ΑΝΩΝΥΜΗ ΕΜΠΟΡΙΚΗ",
        issuedAt = "2026-10-22T11:18",
        cardAmount = "0.00",
        payment = AadeReceipt.Payment.NOT_CARD,
        net = mapOf('Β' to "10.50"),
        vat = mapOf('Β' to "1.37"),
    ),
    PHARMACY(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DMH240001630002855827A380ABF60D0D5982EF4CCB970E6326102014215.09",
        registerId = "DMH24000163",
        receiptNumber = 28558,
        total = "5.09",
        timeHint = "2026-10-20T14:21",
        fixture = "pharmacy",
        afm = "063881871",
        merchantName = "ΤΣΑΡΑ ΜΑΡΙΝ",
        issuedAt = "2026-10-20T14:21",
        cardAmount = "5.09",
        payment = AadeReceipt.Payment.CARD,
        net = mapOf('Α' to "4.80"),
        vat = mapOf('Α' to "0.29"),
    ),

    // Two VAT rates.
    GRILL(
        qr = "https://www1.aade.gr/tameiakes/myweb/q1.php" +
            "?SIG=DNA2501205600025460762BCE415A48AFE0A42D04D0D112E3261017205812.80",
        registerId = "DNA25012056",
        receiptNumber = 25460,
        total = "12.80",
        timeHint = "2026-10-17T20:58",
        fixture = "grill",
        afm = "044136828",
        merchantName = "ΠΑΝΑΓΟΠΟΥΛΟΣ ΧΡΗΣΤ",
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
    "?q=X9WCak9IsJ9cef2a3LrgOGDZibLyAFx0JymA3H6EsnnhFPCeW3i1FtZfxqMaFJBoCfWuNDjqYRDZnS" +
    "%2FsKQvj%2FBPhX1%2FpXXu3SbAhKkydR3g%3D"

fun myDataPage(name: String): String = testResource("mydata/$name.html").readText()

/** A real AADE page: the shared head and tail around one `body_*.html`. */
fun aadePage(body: String, crlf: Boolean = false): String {
    val html = listOf("head.html", "body_$body.html", "tail.html")
        .joinToString("") { testResource("aade/$it").readText().replace("\r\n", "\n") }
    return if (crlf) html.replace("\n", "\r\n") else html
}

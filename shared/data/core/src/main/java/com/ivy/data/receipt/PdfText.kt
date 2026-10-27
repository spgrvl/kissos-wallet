package com.ivy.data.receipt

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * Pulls the text out of simple generated PDFs, like e-invoicing providers' receipts, without a PDF library.
 *
 * Supports what such PDFs use: Flate-compressed streams, fonts with 2-byte glyph codes (Identity-H) mapped
 * to text by ToUnicode CMaps, and text placed with `Tm`. All fonts' CMaps are merged, which works when
 * the fonts are subsets of one family. Returns the text as lines, top to bottom and left to right.
 */
object PdfText {
    /** Guards against compressed streams that expand to absurd sizes. */
    private const val MAX_STREAM_BYTES = 4 * 1024 * 1024
    private const val BUFFER_BYTES = 8 * 1024
    private const val HEX_RADIX = 16
    private const val OCTAL_RADIX = 8
    private const val MAX_OCTAL_DIGITS = 3
    private const val BYTE_MASK = 0xFF
    private const val BITS_PER_BYTE = 8

    private val STREAM_START = Regex("stream\r?\n")
    private val BF_CHAR_BLOCK = Regex("(?s)beginbfchar(.*?)endbfchar")
    private val BF_RANGE_BLOCK = Regex("(?s)beginbfrange(.*?)endbfrange")
    private val BF_CHAR = Regex("<(\\p{XDigit}+)>\\s*<(\\p{XDigit}+)>")
    private val BF_RANGE = Regex("<(\\p{XDigit}+)>\\s*<(\\p{XDigit}+)>\\s*<(\\p{XDigit}+)>")
    private val TEXT_BLOCK = Regex("(?s)BT(.*?)ET")
    private val TEXT_MATRIX = Regex("([-\\d.]+)\\s+([-\\d.]+)\\s+Tm")
    private val LITERAL_STRING = Regex("(?s)\\(((?:\\\\.|[^\\\\)])*)\\)")

    private data class Piece(val y: Long, val x: Double, val text: String)

    fun lines(pdf: ByteArray): List<String> {
        // ISO-8859-1 maps every byte to one char, so regexes can work on the raw bytes
        val streams = streams(String(pdf, Charsets.ISO_8859_1))
        val toUnicode = streams.filter { "beginbfchar" in it || "beginbfrange" in it }
            .fold(mutableMapOf<Int, String>()) { map, cmap -> map.apply { putAll(cmap(cmap)) } }
        return streams.filter { "BT" in it && " Tm" in it }
            .flatMap { pieces(it, toUnicode) }
            .groupBy { it.y }
            .toSortedMap()
            .values
            .map { line -> line.sortedBy { it.x }.joinToString(" ") { it.text }.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun streams(pdf: String): List<String> = STREAM_START.findAll(pdf).mapNotNull { start ->
        val end = pdf.indexOf("endstream", start.range.last)
        if (end < 0) return@mapNotNull null
        inflate(pdf.substring(start.range.last + 1, end).toByteArray(Charsets.ISO_8859_1))
            ?.let { String(it, Charsets.ISO_8859_1) }
    }.toList()

    private fun inflate(data: ByteArray): ByteArray? {
        val inflater = Inflater()
        return try {
            inflater.setInput(data)
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(BUFFER_BYTES)
            while (!inflater.finished() && out.size() < MAX_STREAM_BYTES) {
                val count = inflater.inflate(buffer)
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                out.write(buffer, 0, count)
            }
            out.toByteArray().takeIf { it.isNotEmpty() }
        } catch (e: DataFormatException) {
            null // Not Flate-compressed, e.g. an image
        } finally {
            inflater.end()
        }
    }

    private fun cmap(cmap: String): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        BF_CHAR_BLOCK.findAll(cmap).forEach { block ->
            BF_CHAR.findAll(block.groupValues[1]).forEach {
                map[it.groupValues[1].toInt(HEX_RADIX)] = utf16(it.groupValues[2])
            }
        }
        BF_RANGE_BLOCK.findAll(cmap).forEach { block ->
            BF_RANGE.findAll(block.groupValues[1]).forEach {
                val (first, last, target) = it.destructured.toList().map { hex -> hex.toInt(HEX_RADIX) }
                for (code in first..last) {
                    map[code] = String(Character.toChars(target + code - first))
                }
            }
        }
        return map
    }

    private fun utf16(hex: String): String =
        String(hex.chunked(2).map { it.toInt(HEX_RADIX).toByte() }.toByteArray(), Charsets.UTF_16BE)

    private fun pieces(content: String, toUnicode: Map<Int, String>): List<Piece> =
        TEXT_BLOCK.findAll(content).flatMap { block ->
            val position = TEXT_MATRIX.findAll(block.groupValues[1]).lastOrNull()
            val x = position?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            // PDF y grows upwards; round so that runs on one line share it
            val y = -(position?.groupValues?.get(2)?.toDoubleOrNull() ?: 0.0)
            LITERAL_STRING.findAll(block.groupValues[1]).map {
                Piece(y = Math.round(y), x = x, text = decode(unescape(it.groupValues[1]), toUnicode))
            }
        }.toList()

    /** Glyph codes are 2 bytes each. */
    private fun decode(bytes: ByteArray, toUnicode: Map<Int, String>): String = (0 until bytes.size - 1 step 2)
        .joinToString("") { i ->
            val code = (bytes[i].toInt() and BYTE_MASK shl BITS_PER_BYTE) or (bytes[i + 1].toInt() and BYTE_MASK)
            toUnicode[code].orEmpty()
        }

    @Suppress("CyclomaticComplexMethod")
    private fun unescape(literal: String): ByteArray {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < literal.length) {
            val c = literal[i]
            if (c != '\\' || i + 1 >= literal.length) {
                out.write(c.code)
                i++
                continue
            }
            val next = literal[i + 1]
            when {
                next in "nrtbf" -> {
                    out.write("\n\r\t\b\u000C"["nrtbf".indexOf(next)].code)
                    i += 2
                }

                next in '0'..'7' -> {
                    val digits = literal.substring(i + 1).takeWhile { it in '0'..'7' }.take(MAX_OCTAL_DIGITS)
                    out.write(digits.toInt(OCTAL_RADIX) and BYTE_MASK)
                    i += 1 + digits.length
                }

                else -> {
                    out.write(next.code)
                    i += 2
                }
            }
        }
        return out.toByteArray()
    }
}

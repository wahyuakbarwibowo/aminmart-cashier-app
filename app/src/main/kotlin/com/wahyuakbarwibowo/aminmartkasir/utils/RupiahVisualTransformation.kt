package com.wahyuakbarwibowo.aminmartkasir.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Menampilkan angka dengan pemisah ribuan (titik) di OutlinedTextField,
 * tanpa mengubah nilai mentah (tetap "100000").
 * Contoh: input "100000" tampil "100.000".
 *
 * [allowDecimal] untuk input yang nilainya bisa pecahan (mis. harga satuan
 * hasil bagi harga paket): bagian di belakang titik desimal dibiarkan apa
 * adanya, hanya bagian bulatnya yang diberi pemisah — "1666.6667" tampil
 * "1.666,6667".
 */
class RupiahVisualTransformation(private val allowDecimal: Boolean = false) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val dotIndex = if (allowDecimal) raw.indexOf('.') else -1
        val intPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
        val rest = if (dotIndex >= 0) raw.substring(dotIndex) else ""

        val out = StringBuilder()
        // originalToTransformed[i] = posisi karakter ke-i pada teks tampilan.
        val forward = IntArray(raw.length + 1)
        val intDigits = intPart.count { it.isDigit() }
        var digitsSeen = 0
        for (i in intPart.indices) {
            forward[i] = out.length
            out.append(intPart[i])
            if (intPart[i].isDigit()) {
                digitsSeen++
                val remaining = intDigits - digitsSeen
                if (remaining > 0 && remaining % 3 == 0) out.append('.')
            }
        }
        for (i in rest.indices) {
            forward[intPart.length + i] = out.length
            // Titik desimal ditampilkan sebagai koma agar tidak tertukar dengan pemisah ribuan.
            out.append(if (i == 0 && rest[i] == '.') ',' else rest[i])
        }
        forward[raw.length] = out.length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                forward[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, out.length)
                // Ambil offset asli terkecil yang memetakan ke posisi tampilan ini.
                for (i in 0..raw.length) if (forward[i] >= clamped) return i
                return raw.length
            }
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}

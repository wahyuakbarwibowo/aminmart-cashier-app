package com.wahyuakbarwibowo.aminmartkasir.utils

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class RupiahVisualTransformationTest {

    private fun render(raw: String, allowDecimal: Boolean = false) =
        RupiahVisualTransformation(allowDecimal).filter(AnnotatedString(raw)).text.text

    @Test
    fun `beri pemisah ribuan`() {
        assertEquals("", render(""))
        assertEquals("100", render("100"))
        assertEquals("1.000", render("1000"))
        assertEquals("150.000", render("150000"))
        assertEquals("12.345.678", render("12345678"))
    }

    @Test
    fun `nilai pecahan hanya digrup bagian bulatnya`() {
        assertEquals("1.666,6667", render("1666.6667", allowDecimal = true))
        assertEquals("500,5", render("500.5", allowDecimal = true))
        assertEquals("1.000,", render("1000.", allowDecimal = true))
    }

    @Test
    fun `posisi kursor tetap menunjuk karakter yang sama`() {
        val result = RupiahVisualTransformation().filter(AnnotatedString("1234567"))
        val mapping = result.offsetMapping
        // "1234567" -> "1.234.567"
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(7, mapping.originalToTransformed(5))
        assertEquals(9, mapping.originalToTransformed(7))
        assertEquals(7, mapping.transformedToOriginal(9))
        assertEquals(0, mapping.transformedToOriginal(0))
    }
}

package com.spgrvl.kissos.ui

import io.kotest.matchers.shouldBe
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `greek phone suggests greek`() {
        AppLanguage.suggested("el", emptyList()) shouldBe AppLanguage.GREEK
    }

    @Test
    fun `english phone with a greek sim suggests greek`() {
        AppLanguage.suggested("en", listOf("gr")) shouldBe AppLanguage.GREEK
        AppLanguage.suggested("en", listOf("", "GR")) shouldBe AppLanguage.GREEK
    }

    @Test
    fun `anything else suggests english`() {
        AppLanguage.suggested("en", listOf("gb", "gb")) shouldBe AppLanguage.ENGLISH
        AppLanguage.suggested("de", listOf("de")) shouldBe AppLanguage.ENGLISH
        AppLanguage.suggested(null, emptyList()) shouldBe AppLanguage.ENGLISH
    }
}

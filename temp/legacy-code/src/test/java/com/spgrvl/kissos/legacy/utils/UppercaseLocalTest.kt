package com.spgrvl.kissos.legacy.utils

import io.kotest.matchers.shouldBe
import org.junit.Test

class UppercaseLocalTest {
    @Test
    fun `greek capitals lose their accents`() {
        "Τρίτη, 25 Νοεμβρίου 2026".uppercaseLocal() shouldBe "ΤΡΙΤΗ, 25 ΝΟΕΜΒΡΙΟΥ 2026"
        "Υπόλοιπο λογαριασμού".uppercaseLocal() shouldBe "ΥΠΟΛΟΙΠΟ ΛΟΓΑΡΙΑΣΜΟΥ"
    }

    @Test
    fun `greek capitals keep the dialytika`() {
        "πρωτεΐνη".uppercaseLocal() shouldBe "ΠΡΩΤΕΪΝΗ"
        "Ταΰγετος".uppercaseLocal() shouldBe "ΤΑΫΓΕΤΟΣ"
    }

    @Test
    fun `other languages keep their accents`() {
        "Café Revolut".uppercaseLocal() shouldBe "CAFÉ REVOLUT"
        "Café Αθήνα".uppercaseLocal() shouldBe "CAFÉ ΑΘΗΝΑ"
    }
}

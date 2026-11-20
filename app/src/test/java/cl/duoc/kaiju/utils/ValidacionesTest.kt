package cl.duoc.kaiju.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValidacionesTest : StringSpec({

    "esEnteroValido acepta números enteros, positivos o negativos" {
        "12".esEnteroValido() shouldBe true
        "0".esEnteroValido() shouldBe true
        "-3".esEnteroValido() shouldBe true
    }

    "esEnteroValido rechaza decimales, letras y textos vacíos" {
        "1.5".esEnteroValido() shouldBe false
        "abc".esEnteroValido() shouldBe false
        "12abc".esEnteroValido() shouldBe false
        "".esEnteroValido() shouldBe false
    }

    "esDecimalValido acepta enteros y decimales con punto" {
        "12".esDecimalValido() shouldBe true
        "1500.5".esDecimalValido() shouldBe true
        "-2.5".esDecimalValido() shouldBe true
    }

    "esDecimalValido rechaza letras, textos vacíos y decimales con coma" {
        "abc".esDecimalValido() shouldBe false
        "".esDecimalValido() shouldBe false
        "1,5".esDecimalValido() shouldBe false
    }
})
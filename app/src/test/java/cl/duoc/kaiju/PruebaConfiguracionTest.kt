package cl.duoc.kaiju

import cl.duoc.kaiju.repository.ProductoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

// Prueba mínima: solo comprueba que las librerías de prueba están bien configuradas
class PruebaConfiguracionTest : StringSpec({

    "Kotest, MockK y corrutinas de prueba funcionan juntos" {
        // 1. Simulamos el repository (no usa base de datos real)
        val repository = mockk<ProductoRepository>()
        coEvery { repository.obtenerPorCodigo("A1") } returns null

        // 2. Ejecutamos la función dentro de una corrutina de prueba
        runTest {
            repository.obtenerPorCodigo("A1") shouldBe null
        }
    }
})
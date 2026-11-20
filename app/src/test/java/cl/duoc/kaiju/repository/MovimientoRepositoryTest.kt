package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.local.dao.MovimientoDao
import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.TipoMovimiento
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

private val cafeParaMovimiento = ProductoEntity(
    id = 1, codigo = "P001", nombre = "Café", descripcion = "", categoria = "Bebidas",
    precio = 1500.0, stock = 10, stockMinimo = 3
)

private val movimientoExistente = MovimientoEntity(
    id = 9, productoId = 1, productoNombre = "Café", tipo = "ENTRADA",
    cantidad = 5, stockResultante = 15, motivo = ""
)

// DAOs falsos y una transacción falsa que solo cuenta cuántas veces se usó
private class EntornoRepositorioMovimiento {
    val productoDao = mockk<ProductoDao>(relaxed = true)
    val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    var transacciones = 0
    val repository: MovimientoRepository

    init {
        every { movimientoDao.getAllMovimientos() } returns flowOf(listOf(movimientoExistente))
        repository = MovimientoRepository(productoDao, movimientoDao) { bloque ->
            transacciones++
            bloque()
        }
    }
}

class MovimientoRepositoryTest : StringSpec({

    "el historial de movimientos viene del DAO" {
        val entorno = EntornoRepositorioMovimiento()

        entorno.repository.movimientos.first() shouldBe listOf(movimientoExistente)
    }

    "registrar actualiza el stock del producto y guarda el movimiento en una sola transacción" {
        val entorno = EntornoRepositorioMovimiento()
        val productoActualizado = slot<ProductoEntity>()
        val movimientoGuardado = slot<MovimientoEntity>()

        entorno.repository.registrar(
            producto = cafeParaMovimiento,
            tipo = TipoMovimiento.ENTRADA,
            cantidad = 5,
            nuevoStock = 15,
            motivo = "Compra"
        )

        coVerify(exactly = 1) { entorno.productoDao.update(capture(productoActualizado)) }
        coVerify(exactly = 1) { entorno.movimientoDao.insert(capture(movimientoGuardado)) }
        entorno.transacciones shouldBe 1
        productoActualizado.captured.stock shouldBe 15
        movimientoGuardado.captured.productoId shouldBe 1L
        movimientoGuardado.captured.productoNombre shouldBe "Café"
        movimientoGuardado.captured.tipo shouldBe "ENTRADA"
        movimientoGuardado.captured.cantidad shouldBe 5
        movimientoGuardado.captured.stockResultante shouldBe 15
        movimientoGuardado.captured.motivo shouldBe "Compra"
    }

    "primero se actualiza el stock y después se guarda el movimiento" {
        val entorno = EntornoRepositorioMovimiento()

        entorno.repository.registrar(cafeParaMovimiento, TipoMovimiento.SALIDA, 4, 6, "")

        coVerifyOrder {
            entorno.productoDao.update(any())
            entorno.movimientoDao.insert(any())
        }
    }

    "un ajuste guarda el tipo AJUSTE con su motivo" {
        val entorno = EntornoRepositorioMovimiento()
        val movimientoGuardado = slot<MovimientoEntity>()

        entorno.repository.registrar(cafeParaMovimiento, TipoMovimiento.AJUSTE, 8, 8, "Conteo físico")

        coVerify(exactly = 1) { entorno.movimientoDao.insert(capture(movimientoGuardado)) }
        movimientoGuardado.captured.tipo shouldBe "AJUSTE"
        movimientoGuardado.captured.motivo shouldBe "Conteo físico"
    }

    "si falla al guardar el movimiento, el error llega a quien llamó" {
        val entorno = EntornoRepositorioMovimiento()
        coEvery { entorno.movimientoDao.insert(any()) } throws IllegalStateException("fallo de base de datos")

        shouldThrow<IllegalStateException> {
            entorno.repository.registrar(cafeParaMovimiento, TipoMovimiento.ENTRADA, 5, 15, "")
        }
    }
})
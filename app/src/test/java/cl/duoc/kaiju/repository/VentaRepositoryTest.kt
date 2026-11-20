package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.local.dao.MovimientoDao
import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.dao.VentaDao
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.local.entity.VentaEntity
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

private val cafeParaVender = ProductoEntity(
    id = 1, codigo = "P001", nombre = "Café", descripcion = "", categoria = "Bebidas",
    precio = 1500.0, stock = 10, stockMinimo = 3
)

private val ventaExistente = VentaEntity(
    id = 4, productoId = 1, productoNombre = "Café", categoria = "Bebidas",
    cantidad = 1, precioUnitario = 1500.0, total = 1500.0
)

// DAOs falsos y una transacción falsa que solo cuenta cuántas veces se usó
private class EntornoRepositorioVenta {
    val productoDao = mockk<ProductoDao>(relaxed = true)
    val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    val ventaDao = mockk<VentaDao>(relaxed = true)
    var transacciones = 0
    val repository: VentaRepository

    init {
        every { ventaDao.getAllVentas() } returns flowOf(listOf(ventaExistente))
        repository = VentaRepository(productoDao, movimientoDao, ventaDao) { bloque ->
            transacciones++
            bloque()
        }
    }
}

class VentaRepositoryTest : StringSpec({

    "el historial de ventas viene del DAO" {
        val entorno = EntornoRepositorioVenta()

        entorno.repository.ventas.first() shouldBe listOf(ventaExistente)
    }

    "una venta descuenta el stock, guarda la venta y registra la salida en una sola transacción" {
        val entorno = EntornoRepositorioVenta()
        val productoActualizado = slot<ProductoEntity>()
        val ventaGuardada = slot<VentaEntity>()
        val movimientoGuardado = slot<MovimientoEntity>()

        entorno.repository.registrarVenta(cafeParaVender, cantidad = 3, nuevoStock = 7)

        coVerify(exactly = 1) { entorno.productoDao.update(capture(productoActualizado)) }
        coVerify(exactly = 1) { entorno.ventaDao.insert(capture(ventaGuardada)) }
        coVerify(exactly = 1) { entorno.movimientoDao.insert(capture(movimientoGuardado)) }
        entorno.transacciones shouldBe 1

        productoActualizado.captured.stock shouldBe 7

        ventaGuardada.captured.productoId shouldBe 1L
        ventaGuardada.captured.productoNombre shouldBe "Café"
        ventaGuardada.captured.categoria shouldBe "Bebidas"
        ventaGuardada.captured.cantidad shouldBe 3
        ventaGuardada.captured.precioUnitario shouldBe 1500.0
        ventaGuardada.captured.total shouldBe 4500.0 // 3 x 1500

        movimientoGuardado.captured.tipo shouldBe "SALIDA"
        movimientoGuardado.captured.motivo shouldBe "Venta"
        movimientoGuardado.captured.cantidad shouldBe 3
        movimientoGuardado.captured.stockResultante shouldBe 7
    }

    "las tres operaciones se hacen en orden: stock, venta y movimiento" {
        val entorno = EntornoRepositorioVenta()

        entorno.repository.registrarVenta(cafeParaVender, 2, 8)

        coVerifyOrder {
            entorno.productoDao.update(any())
            entorno.ventaDao.insert(any())
            entorno.movimientoDao.insert(any())
        }
    }

    "si falla al guardar la venta, el error llega a quien llamó" {
        val entorno = EntornoRepositorioVenta()
        coEvery { entorno.ventaDao.insert(any()) } throws IllegalStateException("fallo de base de datos")

        shouldThrow<IllegalStateException> {
            entorno.repository.registrarVenta(cafeParaVender, 2, 8)
        }
    }
})
package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.local.entity.VentaEntity
import cl.duoc.kaiju.model.ItemCategoria
import cl.duoc.kaiju.model.ItemRanking
import cl.duoc.kaiju.repository.ProductoRepository
import cl.duoc.kaiju.repository.VentaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout

private fun productoResumen(id: Long, nombre: String, stock: Int, minimo: Int) = ProductoEntity(
    id = id, codigo = "P$id", nombre = nombre, descripcion = "", categoria = "General",
    precio = 1000.0, stock = stock, stockMinimo = minimo
)

private fun ventaResumen(productoId: Long, nombre: String, categoria: String, cantidad: Int, total: Double) =
    VentaEntity(
        productoId = productoId, productoNombre = nombre, categoria = categoria,
        cantidad = cantidad, precioUnitario = total / cantidad, total = total
    )

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "calcula los totales, los rankings y las ventas por categoría" {
        val productos = listOf(
            productoResumen(1, "Café", stock = 5, minimo = 5), // stock bajo (5 <= 5)
            productoResumen(2, "Té", stock = 20, minimo = 3),
            productoResumen(3, "Azúcar", stock = 8, minimo = 2)
        )
        val ventas = listOf(
            ventaResumen(1, "Café", "Bebidas", cantidad = 3, total = 3000.0),
            ventaResumen(2, "Té", "Bebidas", cantidad = 1, total = 800.0),
            ventaResumen(3, "Azúcar", "Dulces", cantidad = 2, total = 400.0)
        )
        val productoRepository = mockk<ProductoRepository>()
        val ventaRepository = mockk<VentaRepository>()
        every { productoRepository.productos } returns flowOf(productos)
        every { ventaRepository.ventas } returns flowOf(ventas)
        val viewModel = ResumenViewModel(productoRepository, ventaRepository)

        // Se espera a que el resumen se calcule (con un máximo de 5 segundos)
        val estado = withTimeout(5_000) { viewModel.state.first { it.cantidadVentas > 0 } }

        estado.totalVendido shouldBe 4200.0
        estado.unidadesVendidas shouldBe 6
        estado.cantidadVentas shouldBe 3
        estado.productosStockBajo shouldBe 1
        estado.masVendidos shouldBe listOf(
            ItemRanking("Café", 3), ItemRanking("Azúcar", 2), ItemRanking("Té", 1)
        )
        estado.menosVendidos shouldBe listOf(
            ItemRanking("Té", 1), ItemRanking("Azúcar", 2), ItemRanking("Café", 3)
        )
        estado.ventasPorCategoria shouldBe listOf(
            ItemCategoria("Bebidas", 3800.0), ItemCategoria("Dulces", 400.0)
        )
    }

    "sin ventas no hay más vendidos y todos los productos quedan con 0 unidades" {
        val productos = listOf(
            productoResumen(1, "Café", stock = 5, minimo = 5),
            productoResumen(2, "Té", stock = 20, minimo = 3)
        )
        val productoRepository = mockk<ProductoRepository>()
        val ventaRepository = mockk<VentaRepository>()
        every { productoRepository.productos } returns flowOf(productos)
        every { ventaRepository.ventas } returns flowOf(emptyList())
        val viewModel = ResumenViewModel(productoRepository, ventaRepository)

        val estado = withTimeout(5_000) { viewModel.state.first { it.menosVendidos.isNotEmpty() } }

        estado.totalVendido shouldBe 0.0
        estado.masVendidos shouldBe emptyList<ItemRanking>()
        estado.menosVendidos shouldBe listOf(ItemRanking("Café", 0), ItemRanking("Té", 0))
        estado.productosStockBajo shouldBe 1
        estado.ventasPorCategoria shouldBe emptyList<ItemCategoria>()
    }
})
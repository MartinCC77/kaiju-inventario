package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.VentaUiState
import cl.duoc.kaiju.repository.ProductoRepository
import cl.duoc.kaiju.repository.VentaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

private fun productoParaVenta(stock: Int, stockMinimo: Int = 2) = ProductoEntity(
    id = 1, codigo = "P001", nombre = "Café", descripcion = "", categoria = "Bebidas",
    precio = 1500.0, stock = stock, stockMinimo = stockMinimo
)

// Arma el ViewModel con repositories falsos y guarda los avisos de stock bajo que se disparen
private class EntornoVenta(productoEnBase: ProductoEntity?) {
    val productoRepository = mockk<ProductoRepository>()
    val ventaRepository = mockk<VentaRepository>(relaxed = true)
    val avisos = mutableListOf<Pair<ProductoEntity, Int>>()
    val viewModel: VentaViewModel

    init {
        every { productoRepository.productos } returns flowOf(emptyList())
        every { ventaRepository.ventas } returns flowOf(emptyList())
        coEvery { productoRepository.obtenerPorId(1L) } returns productoEnBase
        viewModel = VentaViewModel(productoRepository, ventaRepository) { producto, stock ->
            avisos.add(producto to stock)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class VentaViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "sin producto ni cantidad muestra los errores y no registra la venta" {
        val entorno = EntornoVenta(productoParaVenta(10))
        var seGuardo = false

        entorno.viewModel.registrarVenta { seGuardo = true }

        val errores = entorno.viewModel.state.value.errores
        errores.producto shouldBe "Selecciona un producto"
        errores.cantidad shouldBe "La cantidad es requerida"
        seGuardo shouldBe false
        coVerify(exactly = 0) { entorno.ventaRepository.registrarVenta(any(), any(), any()) }
    }

    "seleccionar un producto guarda su id y su precio" {
        val entorno = EntornoVenta(productoParaVenta(10))

        entorno.viewModel.onProductoSeleccionado(productoParaVenta(10))

        entorno.viewModel.state.value.productoId shouldBe 1L
        entorno.viewModel.state.value.precioUnitario shouldBe 1500.0
    }

    "una cantidad con decimales muestra el error de número entero" {
        val entorno = EntornoVenta(productoParaVenta(10))
        entorno.viewModel.onProductoSeleccionado(productoParaVenta(10))
        entorno.viewModel.onCantidadChanged("1.5")

        entorno.viewModel.registrarVenta { }

        entorno.viewModel.state.value.errores.cantidad shouldBe "Debe ser un número entero"
    }

    "una cantidad igual a 0 muestra el error de cantidad mayor a 0" {
        val entorno = EntornoVenta(productoParaVenta(10))
        entorno.viewModel.onProductoSeleccionado(productoParaVenta(10))
        entorno.viewModel.onCantidadChanged("0")

        entorno.viewModel.registrarVenta { }

        entorno.viewModel.state.value.errores.cantidad shouldBe "La cantidad debe ser mayor a 0"
    }

    "no permite vender más unidades que el stock disponible" {
        val entorno = EntornoVenta(productoParaVenta(stock = 3))
        entorno.viewModel.onProductoSeleccionado(productoParaVenta(stock = 3))
        entorno.viewModel.onCantidadChanged("5")
        var seGuardo = false

        entorno.viewModel.registrarVenta { seGuardo = true }

        entorno.viewModel.state.value.errores.cantidad shouldBe "Stock insuficiente (disponible: 3)"
        seGuardo shouldBe false
        coVerify(exactly = 0) { entorno.ventaRepository.registrarVenta(any(), any(), any()) }
    }

    "una venta válida descuenta el stock, limpia el formulario y no avisa si queda stock suficiente" {
        val producto = productoParaVenta(stock = 10, stockMinimo = 2)
        val entorno = EntornoVenta(producto)
        entorno.viewModel.onProductoSeleccionado(producto)
        entorno.viewModel.onCantidadChanged("3")
        var seGuardo = false

        entorno.viewModel.registrarVenta { seGuardo = true }

        coVerify(exactly = 1) { entorno.ventaRepository.registrarVenta(producto, 3, 7) }
        entorno.avisos shouldBe emptyList<Pair<ProductoEntity, Int>>()
        seGuardo shouldBe true
        entorno.viewModel.state.value shouldBe VentaUiState()
    }

    "si la venta deja el stock en el mínimo o menos, se dispara el aviso de stock bajo" {
        val producto = productoParaVenta(stock = 5, stockMinimo = 2)
        val entorno = EntornoVenta(producto)
        entorno.viewModel.onProductoSeleccionado(producto)
        entorno.viewModel.onCantidadChanged("3")

        entorno.viewModel.registrarVenta { }

        coVerify(exactly = 1) { entorno.ventaRepository.registrarVenta(producto, 3, 2) }
        entorno.avisos shouldBe listOf(producto to 2)
    }

    "si el producto ya no existe en la base no registra la venta" {
        val entorno = EntornoVenta(productoEnBase = null)
        entorno.viewModel.onProductoSeleccionado(productoParaVenta(10))
        entorno.viewModel.onCantidadChanged("1")
        var seGuardo = false

        entorno.viewModel.registrarVenta { seGuardo = true }

        seGuardo shouldBe false
        coVerify(exactly = 0) { entorno.ventaRepository.registrarVenta(any(), any(), any()) }
    }
})
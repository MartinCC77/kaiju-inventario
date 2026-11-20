package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.MovimientoErrores
import cl.duoc.kaiju.model.TipoMovimiento
import cl.duoc.kaiju.repository.MovimientoRepository
import cl.duoc.kaiju.repository.ProductoRepository
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

private fun productoParaMovimiento(stock: Int, stockMinimo: Int = 3) = ProductoEntity(
    id = 1, codigo = "P001", nombre = "Café", descripcion = "", categoria = "Bebidas",
    precio = 1500.0, stock = stock, stockMinimo = stockMinimo
)

// Arma el ViewModel con repositories falsos y guarda los avisos de stock bajo que se disparen
private class EntornoMovimiento(productoEnBase: ProductoEntity?) {
    val productoRepository = mockk<ProductoRepository>()
    val movimientoRepository = mockk<MovimientoRepository>(relaxed = true)
    val avisos = mutableListOf<Pair<ProductoEntity, Int>>()
    val viewModel: MovimientoViewModel

    init {
        every { productoRepository.productos } returns flowOf(emptyList())
        every { movimientoRepository.movimientos } returns flowOf(emptyList())
        coEvery { productoRepository.obtenerPorId(1L) } returns productoEnBase
        viewModel = MovimientoViewModel(productoRepository, movimientoRepository) { producto, stock ->
            avisos.add(producto to stock)
        }
    }

    // Deja el formulario listo con producto, tipo y cantidad
    fun preparar(tipo: TipoMovimiento, cantidad: String, motivo: String = "") {
        viewModel.onProductoSeleccionado(1L)
        viewModel.onTipoChanged(tipo)
        viewModel.onCantidadChanged(cantidad)
        viewModel.onMotivoChanged(motivo)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "sin producto ni cantidad muestra los errores y no registra el movimiento" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.viewModel.onTipoChanged(TipoMovimiento.ENTRADA)
        var seGuardo = false

        entorno.viewModel.registrarMovimiento { seGuardo = true }

        val errores = entorno.viewModel.state.value.errores
        errores.producto shouldBe "Selecciona un producto"
        errores.cantidad shouldBe "La cantidad es requerida"
        seGuardo shouldBe false
    }

    "una cantidad con letras muestra el error de número entero" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.preparar(TipoMovimiento.ENTRADA, "abc")

        entorno.viewModel.registrarMovimiento { }

        entorno.viewModel.state.value.errores.cantidad shouldBe "Debe ser un número entero"
    }

    "una entrada con cantidad 0 muestra el error de cantidad mayor a 0" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.preparar(TipoMovimiento.ENTRADA, "0")

        entorno.viewModel.registrarMovimiento { }

        entorno.viewModel.state.value.errores.cantidad shouldBe "La cantidad debe ser mayor a 0"
    }

    "un ajuste sin motivo muestra el error del motivo" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.preparar(TipoMovimiento.AJUSTE, "5", motivo = "")

        entorno.viewModel.registrarMovimiento { }

        entorno.viewModel.state.value.errores.motivo shouldBe "Indica el motivo del ajuste"
        coVerify(exactly = 0) { entorno.movimientoRepository.registrar(any(), any(), any(), any(), any()) }
    }

    "un ajuste con cantidad negativa muestra el error de stock negativo" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.preparar(TipoMovimiento.AJUSTE, "-1", motivo = "Conteo")

        entorno.viewModel.registrarMovimiento { }

        entorno.viewModel.state.value.errores.cantidad shouldBe "El stock no puede ser negativo"
    }

    "una entrada suma al stock y no dispara aviso" {
        val producto = productoParaMovimiento(stock = 10)
        val entorno = EntornoMovimiento(producto)
        entorno.preparar(TipoMovimiento.ENTRADA, "5")
        var seGuardo = false

        entorno.viewModel.registrarMovimiento { seGuardo = true }

        coVerify(exactly = 1) {
            entorno.movimientoRepository.registrar(producto, TipoMovimiento.ENTRADA, 5, 15, "")
        }
        entorno.avisos shouldBe emptyList<Pair<ProductoEntity, Int>>()
        seGuardo shouldBe true
    }

    "una entrada no avisa aunque el stock siga bajo el mínimo" {
        val producto = productoParaMovimiento(stock = 0, stockMinimo = 5)
        val entorno = EntornoMovimiento(producto)
        entorno.preparar(TipoMovimiento.ENTRADA, "1")

        entorno.viewModel.registrarMovimiento { }

        entorno.avisos shouldBe emptyList<Pair<ProductoEntity, Int>>()
    }

    "una salida mayor al stock disponible muestra el error y no registra" {
        val producto = productoParaMovimiento(stock = 4)
        val entorno = EntornoMovimiento(producto)
        entorno.preparar(TipoMovimiento.SALIDA, "6")
        var seGuardo = false

        entorno.viewModel.registrarMovimiento { seGuardo = true }

        entorno.viewModel.state.value.errores.cantidad shouldBe "Stock insuficiente (disponible: 4)"
        seGuardo shouldBe false
        coVerify(exactly = 0) { entorno.movimientoRepository.registrar(any(), any(), any(), any(), any()) }
    }

    "una salida que deja el stock en el mínimo o menos dispara el aviso" {
        val producto = productoParaMovimiento(stock = 10, stockMinimo = 3)
        val entorno = EntornoMovimiento(producto)
        entorno.preparar(TipoMovimiento.SALIDA, "8")

        entorno.viewModel.registrarMovimiento { }

        coVerify(exactly = 1) {
            entorno.movimientoRepository.registrar(producto, TipoMovimiento.SALIDA, 8, 2, "")
        }
        entorno.avisos shouldBe listOf(producto to 2)
    }

    "un ajuste fija el stock al valor indicado, guarda el motivo sin espacios y avisa si queda bajo" {
        val producto = productoParaMovimiento(stock = 10, stockMinimo = 3)
        val entorno = EntornoMovimiento(producto)
        entorno.preparar(TipoMovimiento.AJUSTE, "1", motivo = " Conteo ")

        entorno.viewModel.registrarMovimiento { }

        coVerify(exactly = 1) {
            entorno.movimientoRepository.registrar(producto, TipoMovimiento.AJUSTE, 1, 1, "Conteo")
        }
        entorno.avisos shouldBe listOf(producto to 1)
    }

    "cambiar el tipo de movimiento borra los errores del formulario" {
        val entorno = EntornoMovimiento(productoParaMovimiento(10))
        entorno.viewModel.onTipoChanged(TipoMovimiento.ENTRADA)
        entorno.viewModel.registrarMovimiento { }
        entorno.viewModel.state.value.errores.producto shouldBe "Selecciona un producto"

        entorno.viewModel.onTipoChanged(TipoMovimiento.SALIDA)

        entorno.viewModel.state.value.errores shouldBe MovimientoErrores()
    }

    "si el producto ya no existe en la base no registra el movimiento" {
        val entorno = EntornoMovimiento(productoEnBase = null)
        entorno.preparar(TipoMovimiento.ENTRADA, "5")
        var seGuardo = false

        entorno.viewModel.registrarMovimiento { seGuardo = true }

        seGuardo shouldBe false
        coVerify(exactly = 0) { entorno.movimientoRepository.registrar(any(), any(), any(), any(), any()) }
    }
})
package cl.duoc.kaiju.viewmodel

import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.ProductoUiState
import cl.duoc.kaiju.repository.ProductoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Repository falso: no usa base de datos. Solo definimos lo que el ViewModel necesita al crearse
private fun crearRepositorioFalso(): ProductoRepository {
    val repository = mockk<ProductoRepository>(relaxed = true)
    every { repository.productos } returns flowOf(emptyList())
    coEvery { repository.obtenerPorCodigo(any()) } returns null // por defecto, el código no existe
    return repository
}

// Deja el formulario con datos válidos (cada prueba cambia solo el dato que quiere probar)
private fun ProductoViewModel.completarFormularioValido() {
    onCodigoChanged("P001")
    onNombreChanged("Café")
    onDescripcionChanged("Café molido")
    onCategoriaChanged("Bebidas")
    onPrecioChanged("1500")
    onStockChanged("10")
    onStockMinimoChanged("3")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en las pruebas
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "con el formulario vacío muestra los errores de campos requeridos y no guarda" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        var seGuardo = false

        viewModel.guardarProducto { seGuardo = true }

        val errores = viewModel.state.value.errores
        errores.codigo shouldBe "El código es requerido"
        errores.nombre shouldBe "El nombre es requerido"
        errores.categoria shouldBe "La categoría es requerida"
        errores.precio shouldBe "El precio es requerido"
        errores.stock shouldBe "El stock es requerido"
        errores.stockMinimo shouldBe "El stock mínimo es requerido"
        seGuardo shouldBe false
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un precio igual a 0 muestra el error de precio mayor a 0" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onPrecioChanged("0")

        viewModel.guardarProducto { }

        viewModel.state.value.errores.precio shouldBe "El precio debe ser mayor a 0"
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un precio con letras muestra el error de número válido" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onPrecioChanged("abc")

        viewModel.guardarProducto { }

        viewModel.state.value.errores.precio shouldBe "Ingresa un número válido"
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un stock negativo muestra el error de stock no negativo" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onStockChanged("-1")

        viewModel.guardarProducto { }

        viewModel.state.value.errores.stock shouldBe "El stock no puede ser negativo"
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un stock con decimales muestra el error de número entero" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onStockChanged("1.5")

        viewModel.guardarProducto { }

        viewModel.state.value.errores.stock shouldBe "Debe ser un número entero"
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un stock mínimo negativo muestra el error correspondiente" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onStockMinimoChanged("-2")

        viewModel.guardarProducto { }

        viewModel.state.value.errores.stockMinimo shouldBe "El stock mínimo no puede ser negativo"
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "no permite guardar un producto con un código que ya existe" {
        val repository = crearRepositorioFalso()
        // Simulamos que en la base de datos ya hay un producto con el código P001
        coEvery { repository.obtenerPorCodigo("P001") } returns ProductoEntity(
            codigo = "P001", nombre = "Existente", descripcion = "",
            categoria = "Bebidas", precio = 1000.0, stock = 5, stockMinimo = 1
        )
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        var seGuardo = false

        viewModel.guardarProducto { seGuardo = true }

        viewModel.state.value.errores.codigo shouldBe "Ya existe un producto con este código"
        seGuardo shouldBe false
        coVerify(exactly = 0) { repository.insertar(any()) }
    }

    "un producto válido se guarda sin espacios sobrantes y limpia el formulario" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.completarFormularioValido()
        viewModel.onCodigoChanged("  P001  ") // con espacios a propósito
        viewModel.onPrecioChanged("1500.5")
        var seGuardo = false

        viewModel.guardarProducto { seGuardo = true }

        // Capturamos el producto que el ViewModel le entregó al repository
        val guardado = slot<ProductoEntity>()
        coVerify(exactly = 1) { repository.insertar(capture(guardado)) }
        guardado.captured.codigo shouldBe "P001"
        guardado.captured.nombre shouldBe "Café"
        guardado.captured.precio shouldBe 1500.5
        guardado.captured.stock shouldBe 10
        guardado.captured.stockMinimo shouldBe 3

        seGuardo shouldBe true
        viewModel.state.value shouldBe ProductoUiState() // el formulario quedó vacío
    }

    "al escribir en un campo con error, el error de ese campo desaparece" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        viewModel.guardarProducto { } // genera los errores de campos vacíos
        viewModel.state.value.errores.codigo shouldBe "El código es requerido"

        viewModel.onCodigoChanged("P001")

        viewModel.state.value.errores.codigo shouldBe null
    }

    "eliminarProducto le pide al repository que elimine ese producto" {
        val repository = crearRepositorioFalso()
        val viewModel = ProductoViewModel(repository)
        val producto = ProductoEntity(
            id = 7, codigo = "P007", nombre = "Té", descripcion = "",
            categoria = "Bebidas", precio = 800.0, stock = 4, stockMinimo = 2
        )

        viewModel.eliminarProducto(producto)

        coVerify(exactly = 1) { repository.eliminar(producto) }
    }
})
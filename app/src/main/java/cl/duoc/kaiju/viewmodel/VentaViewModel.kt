package cl.duoc.kaiju.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.repository.ProductoRepository
import cl.duoc.kaiju.repository.VentaRepository
import cl.duoc.kaiju.model.VentaErrores
import cl.duoc.kaiju.model.VentaUiState
import cl.duoc.kaiju.utils.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Los repositories y la acción de avisar llegan por el constructor ("enchufes")
class VentaViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository,
    // Recibe el producto y su nuevo stock. En la app real muestra la notificación
    private val avisarStockBajo: (ProductoEntity, Int) -> Unit
) : ViewModel() {

    // Productos disponibles para vender
    val productos = productoRepository.productos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Historial de ventas
    val ventas = ventaRepository.ventas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado del formulario
    private val _state = MutableStateFlow(VentaUiState())
    val state: StateFlow<VentaUiState> = _state.asStateFlow()

    //Eventos del formulario

    fun onProductoSeleccionado(producto: ProductoEntity) {
        _state.update {
            it.copy(
                productoId = producto.id,
                precioUnitario = producto.precio,
                errores = it.errores.copy(producto = null)
            )
        }
    }

    fun onCantidadChanged(valor: String) {
        _state.update { it.copy(cantidad = valor, errores = it.errores.copy(cantidad = null)) }
    }

    //Validación

    private fun validar(): Boolean {
        val s = _state.value
        val cantidad = s.cantidad.toIntOrNull()

        val errores = VentaErrores(
            producto = if (s.productoId == null) "Selecciona un producto" else null,
            cantidad = when {
                s.cantidad.isBlank() -> "La cantidad es requerida"
                cantidad == null -> "Debe ser un número entero"
                cantidad <= 0 -> "La cantidad debe ser mayor a 0"
                else -> null
            }
        )

        _state.update { it.copy(errores = errores) }
        return errores == VentaErrores()
    }

    //Acción principal

    fun registrarVenta(onGuardado: () -> Unit) {
        if (!validar()) return

        viewModelScope.launch {
            val s = _state.value
            val cantidad = s.cantidad.toInt()

            // Se lee el producto actual para usar su stock y precio reales
            val producto = productoRepository.obtenerPorId(s.productoId!!) ?: return@launch

            // Regla de negocio: no se puede vender más de lo que hay
            if (cantidad > producto.stock) {
                _state.update {
                    it.copy(
                        errores = it.errores.copy(
                            cantidad = "Stock insuficiente (disponible: ${producto.stock})"
                        )
                    )
                }
                return@launch
            }

            val nuevoStock = producto.stock - cantidad

            // El repository guarda la venta, descuenta el stock y registra la salida
            ventaRepository.registrarVenta(producto, cantidad, nuevoStock)

            // Si el stock quedó en el mínimo o menos, se avisa
            if (nuevoStock <= producto.stockMinimo) {
                avisarStockBajo(producto, nuevoStock)
            }

            _state.value = VentaUiState() // limpia el formulario
            onGuardado()
        }
    }

    // ---------- Fábrica: arma el ViewModel real con la base de datos real ----------
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getInstance(application)

                VentaViewModel(
                    productoRepository = ProductoRepository(database.productoDao),
                    ventaRepository = VentaRepository(database),
                    avisarStockBajo = { producto, nuevoStock ->
                        NotificacionHelper.mostrarStockBajo(
                            context = application,
                            productoId = producto.id,
                            nombre = producto.nombre,
                            stock = nuevoStock,
                            stockMinimo = producto.stockMinimo
                        )
                    }
                )
            }
        }
    }
}
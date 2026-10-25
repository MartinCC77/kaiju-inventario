package cl.duoc.kaiju.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.repository.ProductoRepository
import cl.duoc.kaiju.data.repository.VentaRepository
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


class VentaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val productoRepository = ProductoRepository(database.productoDao)
    private val ventaRepository = VentaRepository(database)

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

            // Si el stock quedó en el mínimo o menos, se avisa con una notificación
            if (nuevoStock <= producto.stockMinimo) {
                NotificacionHelper.mostrarStockBajo(
                    context = getApplication<Application>(),
                    productoId = producto.id,
                    nombre = producto.nombre,
                    stock = nuevoStock,
                    stockMinimo = producto.stockMinimo
                )
            }

            _state.value = VentaUiState() // limpia el formulario
            onGuardado()
        }
    }
}
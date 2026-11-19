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
import cl.duoc.kaiju.data.repository.MovimientoRepository
import cl.duoc.kaiju.data.repository.ProductoRepository
import cl.duoc.kaiju.model.MovimientoErrores
import cl.duoc.kaiju.model.MovimientoUiState
import cl.duoc.kaiju.model.TipoMovimiento
import cl.duoc.kaiju.utils.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Los repositories y la acción de avisar llegan por el constructor ("enchufes")
class MovimientoViewModel(
    private val productoRepository: ProductoRepository,
    private val movimientoRepository: MovimientoRepository,
    // Recibe el producto y su nuevo stock. En la app real muestra la notificación
    private val avisarStockBajo: (ProductoEntity, Int) -> Unit
) : ViewModel() {

    // Productos disponibles para elegir en el formulario
    val productos = productoRepository.productos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Historial de movimientos
    val movimientos = movimientoRepository.movimientos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado del formulario
    private val _state = MutableStateFlow(MovimientoUiState())
    val state: StateFlow<MovimientoUiState> = _state.asStateFlow()

    //Eventos del formulario

    fun onProductoSeleccionado(id: Long) {
        _state.update { it.copy(productoId = id, errores = it.errores.copy(producto = null)) }
    }

    fun onTipoChanged(tipo: TipoMovimiento) {
        _state.update { it.copy(tipo = tipo, errores = MovimientoErrores()) }
    }

    fun onCantidadChanged(valor: String) {
        _state.update { it.copy(cantidad = valor, errores = it.errores.copy(cantidad = null)) }
    }

    fun onMotivoChanged(valor: String) {
        _state.update { it.copy(motivo = valor, errores = it.errores.copy(motivo = null)) }
    }

    //Validación
    private fun validar(): Boolean {
        val s = _state.value
        val cantidad = s.cantidad.toIntOrNull()

        val errores = MovimientoErrores(
            producto = if (s.productoId == null) "Selecciona un producto" else null,
            cantidad = when {
                s.cantidad.isBlank() -> "La cantidad es requerida"
                cantidad == null -> "Debe ser un número entero"
                s.tipo == TipoMovimiento.AJUSTE && cantidad < 0 -> "El stock no puede ser negativo"
                s.tipo != TipoMovimiento.AJUSTE && cantidad <= 0 -> "La cantidad debe ser mayor a 0"
                else -> null
            },
            motivo = if (s.tipo == TipoMovimiento.AJUSTE && s.motivo.isBlank())
                "Indica el motivo del ajuste" else null
        )

        _state.update { it.copy(errores = errores) }
        return errores == MovimientoErrores()
    }

    //Acción principal
    fun registrarMovimiento(onGuardado: () -> Unit) {
        if (!validar()) return

        viewModelScope.launch {
            val s = _state.value
            val cantidad = s.cantidad.toInt()

            // Se lee el producto actual para usar su stock real
            val producto = productoRepository.obtenerPorId(s.productoId!!) ?: return@launch

            val nuevoStock = when (s.tipo) {
                TipoMovimiento.ENTRADA -> producto.stock + cantidad
                TipoMovimiento.SALIDA -> producto.stock - cantidad
                TipoMovimiento.AJUSTE -> cantidad
            }

            // Regla de negocio: no se puede sacar más de lo que hay
            if (nuevoStock < 0) {
                _state.update {
                    it.copy(
                        errores = it.errores.copy(
                            cantidad = "Stock insuficiente (disponible: ${producto.stock})"
                        )
                    )
                }
                return@launch
            }

            movimientoRepository.registrar(
                producto = producto,
                tipo = s.tipo,
                cantidad = cantidad,
                nuevoStock = nuevoStock,
                motivo = s.motivo.trim()
            )

            // Si fue una salida o ajuste y el stock quedó en el mínimo o menos, se avisa
            if (s.tipo != TipoMovimiento.ENTRADA && nuevoStock <= producto.stockMinimo) {
                avisarStockBajo(producto, nuevoStock)
            }

            _state.value = MovimientoUiState() // limpia el formulario
            onGuardado()
        }
    }

    // ---------- Fábrica: arma el ViewModel real con la base de datos real ----------
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getInstance(application)

                MovimientoViewModel(
                    productoRepository = ProductoRepository(database.productoDao),
                    movimientoRepository = MovimientoRepository(database),
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
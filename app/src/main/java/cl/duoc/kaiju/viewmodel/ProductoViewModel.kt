package cl.duoc.kaiju.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.ProductoErrores
import cl.duoc.kaiju.model.ProductoUiState
import cl.duoc.kaiju.utils.esDecimalValido
import cl.duoc.kaiju.utils.esEnteroValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    // Base de datos local
    private val database = AppDatabase.getInstance(application)

    private val productoDao = database.productoDao

    // Lista de productos: se actualiza sola cuando cambia la base de datos
    val productos = productoDao.getAllProductos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado del formulario
    private val _state = MutableStateFlow(ProductoUiState())
    val state: StateFlow<ProductoUiState> = _state.asStateFlow()

    // Eventos: cada vez que el usuario escribe
    // Al escribir, se limpia el error de ese campo.

    fun onCodigoChanged(valor: String) {
        _state.update { it.copy(codigo = valor, errores = it.errores.copy(codigo = null)) }
    }

    fun onNombreChanged(valor: String) {
        _state.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    }

    fun onDescripcionChanged(valor: String) {
        _state.update { it.copy(descripcion = valor) }
    }

    fun onCategoriaChanged(valor: String) {
        _state.update { it.copy(categoria = valor, errores = it.errores.copy(categoria = null)) }
    }

    fun onPrecioChanged(valor: String) {
        _state.update { it.copy(precio = valor, errores = it.errores.copy(precio = null)) }
    }

    fun onStockChanged(valor: String) {
        _state.update { it.copy(stock = valor, errores = it.errores.copy(stock = null)) }
    }

    fun onStockMinimoChanged(valor: String) {
        _state.update { it.copy(stockMinimo = valor, errores = it.errores.copy(stockMinimo = null)) }
    }

    //Validación

    private fun validar(): Boolean {
        val s = _state.value

        val errores = ProductoErrores(
            codigo = if (s.codigo.isBlank()) "El código es requerido" else null,
            nombre = if (s.nombre.isBlank()) "El nombre es requerido" else null,
            categoria = if (s.categoria.isBlank()) "La categoría es requerida" else null,
            precio = when {
                s.precio.isBlank() -> "El precio es requerido"
                !s.precio.esDecimalValido() -> "Ingresa un número válido"
                s.precio.toDouble() <= 0 -> "El precio debe ser mayor a 0"
                else -> null
            },
            stock = when {
                s.stock.isBlank() -> "El stock es requerido"
                !s.stock.esEnteroValido() -> "Debe ser un número entero"
                s.stock.toInt() < 0 -> "El stock no puede ser negativo"
                else -> null
            },
            stockMinimo = when {
                s.stockMinimo.isBlank() -> "El stock mínimo es requerido"
                !s.stockMinimo.esEnteroValido() -> "Debe ser un número entero"
                s.stockMinimo.toInt() < 0 -> "El stock mínimo no puede ser negativo"
                else -> null
            }
        )

        _state.update { it.copy(errores = errores) }
        return errores == ProductoErrores()
    }

    //Acciones

    fun guardarProducto(onGuardado: () -> Unit) {
        if (!validar()) return

        viewModelScope.launch {
            val s = _state.value

            // No permite dos productos con el mismo código
            if (productoDao.getProductoByCodigo(s.codigo.trim()) != null) {
                _state.update {
                    it.copy(errores = it.errores.copy(codigo = "Ya existe un producto con este código"))
                }
                return@launch
            }

            productoDao.insert(
                ProductoEntity(
                    codigo = s.codigo.trim(),
                    nombre = s.nombre.trim(),
                    descripcion = s.descripcion.trim(),
                    categoria = s.categoria.trim(),
                    precio = s.precio.toDouble(),
                    stock = s.stock.toInt(),
                    stockMinimo = s.stockMinimo.toInt()
                )
            )

            _state.value = ProductoUiState() //limpia el formulario
            onGuardado()
        }
    }

    fun eliminarProducto(producto: ProductoEntity) {
        viewModelScope.launch { productoDao.delete(producto) }
    }
}
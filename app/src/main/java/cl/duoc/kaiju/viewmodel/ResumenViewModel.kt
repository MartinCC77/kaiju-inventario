package cl.duoc.kaiju.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.local.entity.VentaEntity
import cl.duoc.kaiju.data.repository.ProductoRepository
import cl.duoc.kaiju.data.repository.VentaRepository
import cl.duoc.kaiju.model.ItemCategoria
import cl.duoc.kaiju.model.ItemRanking
import cl.duoc.kaiju.model.ResumenUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ResumenViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val productoRepository = ProductoRepository(database.productoDao)
    private val ventaRepository = VentaRepository(database)

    // Se recalcula automáticamente cuando cambian los productos o las ventas
    val state: StateFlow<ResumenUiState> =
        combine<List<ProductoEntity>, List<VentaEntity>, ResumenUiState>(
            productoRepository.productos,
            ventaRepository.ventas
        ) { productos, ventas ->
            calcular(productos, ventas)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResumenUiState()
        )

    private fun calcular(
        productos: List<ProductoEntity>,
        ventas: List<VentaEntity>
    ): ResumenUiState {

        // Unidades vendidas por cada producto
        val unidadesPorProducto = ventas
            .groupBy { it.productoId }
            .mapValues { entrada -> entrada.value.sumOf { it.cantidad } }

        // Ranking de todos los productos (los sin ventas quedan con 0)
        val ranking = productos.map {
            ItemRanking(nombre = it.nombre, unidades = unidadesPorProducto[it.id] ?: 0)
        }

        return ResumenUiState(
            totalVendido = ventas.sumOf { it.total },
            unidadesVendidas = ventas.sumOf { it.cantidad },
            cantidadVentas = ventas.size,
            productosStockBajo = productos.count { it.stock <= it.stockMinimo },
            masVendidos = ranking
                .filter { it.unidades > 0 }
                .sortedByDescending { it.unidades }
                .take(3),
            menosVendidos = ranking
                .sortedBy { it.unidades }
                .take(3),
            ventasPorCategoria = ventas
                .groupBy { it.categoria }
                .map { ItemCategoria(it.key, it.value.sumOf { venta -> venta.total }) }
                .sortedByDescending { it.total }
        )
    }
}
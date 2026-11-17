package cl.duoc.kaiju.data.repository

import androidx.room.withTransaction
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.local.entity.VentaEntity
import cl.duoc.kaiju.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val database: AppDatabase) {

    private val productoDao = database.productoDao
    private val movimientoDao = database.movimientoDao
    private val ventaDao = database.ventaDao

    val ventas: Flow<List<VentaEntity>> = ventaDao.getAllVentas()

    suspend fun registrarVenta(producto: ProductoEntity, cantidad: Int, nuevoStock: Int) {
        database.withTransaction {
            productoDao.update(producto.copy(stock = nuevoStock))

            ventaDao.insert(
                VentaEntity(
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    categoria = producto.categoria,
                    cantidad = cantidad,
                    precioUnitario = producto.precio,
                    total = producto.precio * cantidad
                )
            )

            movimientoDao.insert(
                MovimientoEntity(
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    tipo = TipoMovimiento.SALIDA.name,
                    cantidad = cantidad,
                    stockResultante = nuevoStock,
                    motivo = "Venta"
                )
            )
        }
    }
}
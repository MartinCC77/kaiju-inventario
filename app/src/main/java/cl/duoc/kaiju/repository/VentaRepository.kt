package cl.duoc.kaiju.repository

import androidx.room.withTransaction
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.dao.MovimientoDao
import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.dao.VentaDao
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.data.local.entity.VentaEntity
import cl.duoc.kaiju.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao,
    private val ventaDao: VentaDao,
    // Ejecuta un bloque de operaciones como una sola transacción ("todo o nada")
    private val enTransaccion: suspend (suspend () -> Unit) -> Unit
) {

    // Constructor que usa la app real: toma los DAO y la transacción de la base de datos
    constructor(database: AppDatabase) : this(
        database.productoDao,
        database.movimientoDao,
        database.ventaDao,
        { bloque -> database.withTransaction { bloque() } }
    )

    val ventas: Flow<List<VentaEntity>> = ventaDao.getAllVentas()

    suspend fun registrarVenta(producto: ProductoEntity, cantidad: Int, nuevoStock: Int) {
        // Descontar el stock, guardar la venta y registrar la salida van juntos en una transacción
        enTransaccion {
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
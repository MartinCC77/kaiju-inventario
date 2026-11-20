package cl.duoc.kaiju.repository

import androidx.room.withTransaction
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.dao.MovimientoDao
import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao,
    // Ejecuta un bloque de operaciones como una sola transacción ("todo o nada")
    private val enTransaccion: suspend (suspend () -> Unit) -> Unit
) {

    // Constructor que usa la app real: toma los DAO y la transacción de la base de datos
    constructor(database: AppDatabase) : this(
        database.productoDao,
        database.movimientoDao,
        { bloque -> database.withTransaction { bloque() } }
    )

    val movimientos: Flow<List<MovimientoEntity>> = movimientoDao.getAllMovimientos()

    suspend fun registrar(
        producto: ProductoEntity,
        tipo: TipoMovimiento,
        cantidad: Int,
        nuevoStock: Int,
        motivo: String
    ) {
        // Actualizar el stock y guardar el movimiento van juntos: si falla uno, no se guarda ninguno
        enTransaccion {
            productoDao.update(producto.copy(stock = nuevoStock))
            movimientoDao.insert(
                MovimientoEntity(
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    tipo = tipo.name,
                    cantidad = cantidad,
                    stockResultante = nuevoStock,
                    motivo = motivo
                )
            )
        }
    }
}
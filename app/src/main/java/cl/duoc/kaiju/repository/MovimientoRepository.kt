package cl.duoc.kaiju.data.repository

import androidx.room.withTransaction
import cl.duoc.kaiju.data.local.AppDatabase
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import cl.duoc.kaiju.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(private val database: AppDatabase) {

    private val productoDao = database.productoDao
    private val movimientoDao = database.movimientoDao

    val movimientos: Flow<List<MovimientoEntity>> = movimientoDao.getAllMovimientos()


    suspend fun registrar(
        producto: ProductoEntity,
        tipo: TipoMovimiento,
        cantidad: Int,
        nuevoStock: Int,
        motivo: String
    ) {
        database.withTransaction {
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
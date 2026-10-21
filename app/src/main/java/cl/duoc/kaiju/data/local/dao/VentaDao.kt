package cl.duoc.kaiju.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.kaiju.data.local.entity.VentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    // Historial de ventas, de la más reciente a la más antigua
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getAllVentas(): Flow<List<VentaEntity>>

    @Insert
    suspend fun insert(venta: VentaEntity)
}
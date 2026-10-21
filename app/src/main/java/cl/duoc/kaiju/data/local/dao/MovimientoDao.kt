package cl.duoc.kaiju.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    // Historial completo, del más reciente al más antiguo
    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun getAllMovimientos(): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun insert(movimiento: MovimientoEntity)
}
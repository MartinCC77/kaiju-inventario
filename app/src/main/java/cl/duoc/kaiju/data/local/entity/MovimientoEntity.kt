package cl.duoc.kaiju.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productoId: Long,           // Producto al que pertenece el movimiento
    val productoNombre: String,     // Nombre guardado para mostrarlo en el historial
    val tipo: String,               // "ENTRADA", "SALIDA" o "AJUSTE"
    val cantidad: Int,              // Cantidad ingresada por el usuario
    val stockResultante: Int,       // Stock que quedó después del movimiento
    val motivo: String,             // Motivo u observación
    val fecha: Long = System.currentTimeMillis()
)
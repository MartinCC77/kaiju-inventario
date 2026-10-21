package cl.duoc.kaiju.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productoId: Long,
    val productoNombre: String,   // Nombre guardado para mostrarlo en el historial
    val categoria: String,        // Se guarda para analizar ventas por categoría
    val cantidad: Int,
    val precioUnitario: Double,   // Precio al momento de la venta
    val total: Double,            // cantidad x precioUnitario
    val fecha: Long = System.currentTimeMillis()
)
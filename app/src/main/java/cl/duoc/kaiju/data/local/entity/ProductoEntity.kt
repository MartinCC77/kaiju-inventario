package cl.duoc.kaiju.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,           // Identificador único autoincremental
    val codigo: String,         // Código del producto (ej: "P001")
    val nombre: String,         // Nombre del producto
    val descripcion: String,    // Descripción opcional
    val categoria: String,      // Categoría (ej: "Bebidas")
    val precio: Double,         // Precio de venta
    val stock: Int,             // Cantidad disponible
    val stockMinimo: Int,       // Bajo este número se considera "stock bajo"
    val createdAt: Long = System.currentTimeMillis() // Fecha de creación
)
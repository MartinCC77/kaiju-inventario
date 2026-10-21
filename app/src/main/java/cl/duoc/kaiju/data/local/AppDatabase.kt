package cl.duoc.kaiju.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.entity.ProductoEntity

@Database(
    entities = [ProductoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Acceso a la tabla de productos
    abstract val productoDao: ProductoDao
}
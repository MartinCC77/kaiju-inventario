package cl.duoc.kaiju.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // Lista todos los productos ordenados por nombre (se actualiza en tiempo real)
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAllProductos(): Flow<List<ProductoEntity>>

    // Busca un producto por su id
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getProductoById(id: Long): ProductoEntity?

    // Busca un producto por su código (sirve para evitar códigos repetidos)
    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun getProductoByCodigo(codigo: String): ProductoEntity?

    @Insert
    suspend fun insert(producto: ProductoEntity)

    @Update
    suspend fun update(producto: ProductoEntity)

    @Delete
    suspend fun delete(producto: ProductoEntity)
}
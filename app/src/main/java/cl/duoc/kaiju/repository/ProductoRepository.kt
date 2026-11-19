package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val productoDao: ProductoDao) {
    val productos: Flow<List<ProductoEntity>> = productoDao.getAllProductos()

    suspend fun obtenerPorId(id: Long): ProductoEntity? = productoDao.getProductoById(id)

    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity? =
        productoDao.getProductoByCodigo(codigo)

    suspend fun insertar(producto: ProductoEntity) = productoDao.insert(producto)

    suspend fun actualizar(producto: ProductoEntity) = productoDao.update(producto)

    suspend fun eliminar(producto: ProductoEntity) = productoDao.delete(producto)
}
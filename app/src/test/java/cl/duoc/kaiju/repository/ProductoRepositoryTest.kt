package cl.duoc.kaiju.repository

import cl.duoc.kaiju.data.local.dao.ProductoDao
import cl.duoc.kaiju.data.local.entity.ProductoEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

private val cafe = ProductoEntity(
    id = 5, codigo = "P005", nombre = "Café", descripcion = "", categoria = "Bebidas",
    precio = 1500.0, stock = 10, stockMinimo = 3
)

// DAO falso: no usa base de datos, solo registra qué le piden
private fun crearDaoFalso(): ProductoDao {
    val dao = mockk<ProductoDao>(relaxed = true)
    every { dao.getAllProductos() } returns flowOf(listOf(cafe))
    return dao
}

class ProductoRepositoryTest : StringSpec({

    "la lista de productos viene del DAO" {
        val repository = ProductoRepository(crearDaoFalso())

        repository.productos.first() shouldBe listOf(cafe)
    }

    "obtenerPorId busca el producto por su id" {
        val dao = crearDaoFalso()
        coEvery { dao.getProductoById(5L) } returns cafe

        ProductoRepository(dao).obtenerPorId(5L) shouldBe cafe
    }

    "obtenerPorCodigo devuelve null si el código no existe" {
        val dao = crearDaoFalso()
        coEvery { dao.getProductoByCodigo("NO-EXISTE") } returns null

        ProductoRepository(dao).obtenerPorCodigo("NO-EXISTE") shouldBe null
    }

    "insertar le pide al DAO que guarde el producto" {
        val dao = crearDaoFalso()

        ProductoRepository(dao).insertar(cafe)

        coVerify(exactly = 1) { dao.insert(cafe) }
    }

    "actualizar le pide al DAO que modifique el producto" {
        val dao = crearDaoFalso()

        ProductoRepository(dao).actualizar(cafe)

        coVerify(exactly = 1) { dao.update(cafe) }
    }

    "eliminar le pide al DAO que borre el producto" {
        val dao = crearDaoFalso()

        ProductoRepository(dao).eliminar(cafe)

        coVerify(exactly = 1) { dao.delete(cafe) }
    }
})
package cl.duoc.kaiju.model


data class ProductoErrores(
    val codigo: String? = null,
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: String? = null,
    val stock: String? = null,
    val stockMinimo: String? = null
)
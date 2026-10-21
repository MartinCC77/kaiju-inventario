package cl.duoc.kaiju.model

data class ProductoUiState(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: String = "",
    val stock: String = "",
    val stockMinimo: String = "",
    val errores: ProductoErrores = ProductoErrores()
)
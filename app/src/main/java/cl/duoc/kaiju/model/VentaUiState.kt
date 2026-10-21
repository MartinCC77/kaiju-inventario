package cl.duoc.kaiju.model

data class VentaErrores(
    val producto: String? = null,
    val cantidad: String? = null
)

data class VentaUiState(
    val productoId: Long? = null,
    val precioUnitario: Double = 0.0,
    val cantidad: String = "",
    val errores: VentaErrores = VentaErrores()
) {
    //Total calculado a partir de la cantidad y el precio (0 si la cantidad no es válida)
    val total: Double
        get() = (cantidad.toIntOrNull() ?: 0).coerceAtLeast(0) * precioUnitario
}
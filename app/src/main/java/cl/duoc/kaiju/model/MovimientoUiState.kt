package cl.duoc.kaiju.model

data class MovimientoErrores(
    val producto: String? = null,
    val cantidad: String? = null,
    val motivo: String? = null
)

data class MovimientoUiState(
    val productoId: Long? = null,
    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,
    val cantidad: String = "",
    val motivo: String = "",
    val errores: MovimientoErrores = MovimientoErrores()
)
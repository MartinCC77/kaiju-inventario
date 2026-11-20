package cl.duoc.kaiju.model

data class IndicadoresUiState(
    val cargando: Boolean = false,
    val dolar: Double? = null,
    val euro: Double? = null,
    val uf: Double? = null,
    val utm: Double? = null,
    val fecha: String? = null, // fecha del valor del dólar (AAAA-MM-DD)
    val mensaje: String? = null // error al cargar
)
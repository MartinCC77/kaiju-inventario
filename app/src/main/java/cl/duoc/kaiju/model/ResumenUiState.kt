package cl.duoc.kaiju.model

data class ItemRanking(
    val nombre: String,
    val unidades: Int
)

data class ItemCategoria(
    val categoria: String,
    val total: Double
)

data class ResumenUiState(
    val totalVendido: Double = 0.0,
    val unidadesVendidas: Int = 0,
    val cantidadVentas: Int = 0,
    val productosStockBajo: Int = 0,
    val masVendidos: List<ItemRanking> = emptyList(),
    val menosVendidos: List<ItemRanking> = emptyList(),
    val ventasPorCategoria: List<ItemCategoria> = emptyList()
)
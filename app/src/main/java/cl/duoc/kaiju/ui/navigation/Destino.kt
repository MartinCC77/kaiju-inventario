package cl.duoc.kaiju.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector


enum class Destino(val titulo: String, val icono: ImageVector) {
    PRODUCTOS("Productos", Icons.Filled.Inventory2),
    MOVIMIENTOS("Movimientos", Icons.Filled.SwapVert),
    VENTAS("Ventas", Icons.Filled.ShoppingCart),
    RESUMEN("Resumen", Icons.Filled.BarChart)
}
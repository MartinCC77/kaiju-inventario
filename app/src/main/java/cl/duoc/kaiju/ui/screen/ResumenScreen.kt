package cl.duoc.kaiju.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.kaiju.ui.components.BarraProgreso
import cl.duoc.kaiju.viewmodel.ResumenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    viewModel: ResumenViewModel = viewModel()
) {
    val estado by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumen") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila 1 de indicadores
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaIndicador(
                        modifier = Modifier.weight(1f),
                        titulo = "Total vendido",
                        valor = "$${"%.0f".format(estado.totalVendido)}",
                        icono = Icons.Filled.AttachMoney
                    )
                    TarjetaIndicador(
                        modifier = Modifier.weight(1f),
                        titulo = "Unidades vendidas",
                        valor = "${estado.unidadesVendidas}",
                        icono = Icons.Filled.Inventory2
                    )
                }
            }
            // Fila 2 de indicadores
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaIndicador(
                        modifier = Modifier.weight(1f),
                        titulo = "Ventas realizadas",
                        valor = "${estado.cantidadVentas}",
                        icono = Icons.Filled.ReceiptLong
                    )
                    TarjetaIndicador(
                        modifier = Modifier.weight(1f),
                        titulo = "Con stock bajo",
                        valor = "${estado.productosStockBajo}",
                        icono = Icons.Filled.Warning
                    )
                }
            }

            // Productos más vendidos
            item {
                Text("Más vendidos", style = MaterialTheme.typography.titleLarge)
            }
            if (estado.masVendidos.isEmpty()) {
                item { Text("Aún no hay ventas registradas.") }
            } else {
                val maximo = estado.masVendidos.maxOf { it.unidades }
                items(estado.masVendidos) { producto ->
                    BarraProgreso(
                        etiqueta = producto.nombre,
                        valor = "${producto.unidades} un.",
                        fraccion = producto.unidades.toFloat() / maximo
                    )
                }
            }

            // Productos menos vendidos
            item {
                Text("Menos vendidos", style = MaterialTheme.typography.titleLarge)
            }
            if (estado.menosVendidos.isEmpty()) {
                item { Text("Aún no hay productos registrados.") }
            } else {
                val maximo = estado.menosVendidos.maxOf { it.unidades }.coerceAtLeast(1)
                items(estado.menosVendidos) { producto ->
                    BarraProgreso(
                        etiqueta = producto.nombre,
                        valor = "${producto.unidades} un.",
                        fraccion = producto.unidades.toFloat() / maximo
                    )
                }
            }

            // Ventas por categoría
            item {
                Text("Ventas por categoría", style = MaterialTheme.typography.titleLarge)
            }
            if (estado.ventasPorCategoria.isEmpty()) {
                item { Text("Aún no hay ventas registradas.") }
            } else {
                val maximo = estado.ventasPorCategoria.maxOf { it.total }.coerceAtLeast(1.0)
                items(estado.ventasPorCategoria) { categoria ->
                    BarraProgreso(
                        etiqueta = categoria.categoria,
                        valor = "$${"%.0f".format(categoria.total)}",
                        fraccion = (categoria.total / maximo).toFloat()
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaIndicador(
    modifier: Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(valor, style = MaterialTheme.typography.headlineSmall)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}
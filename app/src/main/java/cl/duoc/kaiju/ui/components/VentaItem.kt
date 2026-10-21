package cl.duoc.kaiju.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.kaiju.data.local.entity.VentaEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VentaItem(venta: VentaEntity) {
    val fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(venta.fecha))

    val precio = "%.0f".format(venta.precioUnitario)
    val total = "%.0f".format(venta.total)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Venta",
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(venta.productoNombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${venta.cantidad} x $$precio = $$total",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("${venta.categoria} · $fecha", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
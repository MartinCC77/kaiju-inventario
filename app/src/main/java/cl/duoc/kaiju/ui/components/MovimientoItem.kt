package cl.duoc.kaiju.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.kaiju.data.local.entity.MovimientoEntity
import cl.duoc.kaiju.model.TipoMovimiento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun MovimientoItem(movimiento: MovimientoEntity) {
    val tipo = TipoMovimiento.valueOf(movimiento.tipo)

    //Icono y texto de cantidad según el tipo de movimiento
    val icono = when (tipo) {
        TipoMovimiento.ENTRADA -> Icons.Filled.ArrowDownward
        TipoMovimiento.SALIDA -> Icons.Filled.ArrowUpward
        TipoMovimiento.AJUSTE -> Icons.Filled.Tune
    }
    val textoCantidad = when (tipo) {
        TipoMovimiento.ENTRADA -> "+${movimiento.cantidad}"
        TipoMovimiento.SALIDA -> "-${movimiento.cantidad}"
        TipoMovimiento.AJUSTE -> "= ${movimiento.cantidad}"
    }
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(movimiento.fecha))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icono, contentDescription = tipo.etiqueta, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(movimiento.productoNombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${tipo.etiqueta} $textoCantidad · Stock quedó en ${movimiento.stockResultante}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (movimiento.motivo.isNotBlank()) {
                    Text("Motivo: ${movimiento.motivo}", style = MaterialTheme.typography.bodySmall)
                }
                Text(fecha, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
package cl.duoc.kaiju.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.kaiju.viewmodel.IndicadoresViewModel
import java.util.Locale

// Tarjeta con los indicadores económicos de la API externa y la conversión del total vendido
@Composable
fun TarjetaIndicadoresEconomicos(
    totalVendido: Double,
    viewModel: IndicadoresViewModel = viewModel(factory = IndicadoresViewModel.Factory)
) {
    val estado by viewModel.state.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("  Indicadores económicos", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = viewModel::cargar) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar indicadores")
                }
            }

            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Si el servicio externo falla, solo esta tarjeta muestra el aviso
            estado.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            estado.dolar?.let { FilaIndicador("Dólar observado", "$ ${formatear(it)}") }
            estado.euro?.let { FilaIndicador("Euro", "$ ${formatear(it)}") }
            estado.uf?.let { FilaIndicador("UF", "$ ${formatear(it)}") }
            estado.utm?.let { FilaIndicador("UTM", "$ ${formatear(it)}") }

            // Conversión del total vendido (dato local) a dólares (dato externo)
            val dolar = estado.dolar
            if (dolar != null && dolar > 0 && totalVendido > 0) {
                FilaIndicador("Total vendido en dólares", "≈ US$ ${formatear(totalVendido / dolar)}")
            }

            estado.fecha?.let {
                Text("Fuente: mindicador.cl · $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FilaIndicador(nombre: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(nombre, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.titleSmall)
    }
}

// Formato chileno: 1.234,56
private fun formatear(valor: Double): String =
    String.format(Locale.forLanguageTag("es-CL"), "%,.2f", valor)
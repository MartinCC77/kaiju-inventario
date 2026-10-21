package cl.duoc.kaiju.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


@Composable
fun BarraProgreso(
    etiqueta: String,
    valor: String,
    fraccion: Float
) {
    // La barra crece suavemente en 800 ms hasta su valor final
    val progresoAnimado by animateFloatAsState(
        targetValue = fraccion.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progresoBarra"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text(valor, style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
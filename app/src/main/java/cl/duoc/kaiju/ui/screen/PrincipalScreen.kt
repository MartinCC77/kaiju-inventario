package cl.duoc.kaiju.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.duoc.kaiju.ui.components.PantallaPendiente
import cl.duoc.kaiju.ui.navigation.Destino


@Composable
fun PrincipalScreen(onCerrarSesion: () -> Unit) {
    // Sección activa (se conserva al rotar la pantalla)
    var destinoActual by rememberSaveable { mutableStateOf(Destino.PRODUCTOS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destino.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = destino == destinoActual,
                        onClick = { destinoActual = destino },
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        // consumeWindowInsets evita que el espacio se aplique dos veces
        Box(modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            // Crossfade: animación de desvanecimiento al cambiar de sección
            Crossfade(targetState = destinoActual, label = "cambioDeSeccion") { destino ->
                when (destino) {
                    Destino.PRODUCTOS -> ProductoScreen(onCerrarSesion = onCerrarSesion)
                    Destino.MOVIMIENTOS -> MovimientoScreen()
                    Destino.VENTAS -> VentaScreen()
                    Destino.RESUMEN -> ResumenScreen()
                }
            }
        }
    }
}
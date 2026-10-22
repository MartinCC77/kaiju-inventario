package cl.duoc.kaiju.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cl.duoc.kaiju.ui.navigation.Destino
import cl.duoc.kaiju.utils.NotificacionHelper


@Composable
fun PrincipalScreen(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current

    // Sección activa (se conserva al rotar la pantalla)
    var destinoActual by rememberSaveable { mutableStateOf(Destino.PRODUCTOS) }

    // Lanzador del cuadro del sistema que pide el permiso (si lo rechaza, la app sigue igual)
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // Al entrar a la app, si falta el permiso de notificaciones, se pide
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificacionHelper.tienePermiso(context)
        ) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
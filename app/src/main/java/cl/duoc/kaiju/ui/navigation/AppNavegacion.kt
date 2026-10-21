package cl.duoc.kaiju.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cl.duoc.kaiju.ui.screen.LoginScreen
import cl.duoc.kaiju.ui.screen.PrincipalScreen


@Composable
fun AppNavegacion() {
    var sesionIniciada by rememberSaveable { mutableStateOf(false) }

    if (sesionIniciada) {
        PrincipalScreen(onCerrarSesion = { sesionIniciada = false })
    } else {
        LoginScreen(onLoginExitoso = { sesionIniciada = true })
    }
}
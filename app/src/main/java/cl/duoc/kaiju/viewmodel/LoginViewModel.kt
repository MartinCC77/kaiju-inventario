package cl.duoc.kaiju.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.kaiju.model.LoginErrores
import cl.duoc.kaiju.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // Credenciales de prueba (solo para el MVP académico)
    private val usuarioDemo = "admin"
    private val claveDemo = "kaiju123"

    fun onUsuarioChanged(valor: String) {
        _state.update {
            it.copy(usuario = valor, errores = it.errores.copy(usuario = null, credenciales = null))
        }
    }

    fun onClaveChanged(valor: String) {
        _state.update {
            it.copy(clave = valor, errores = it.errores.copy(clave = null, credenciales = null))
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val s = _state.value

        // 1. Validar que los campos tengan formato correcto
        val errores = LoginErrores(
            usuario = if (s.usuario.isBlank()) "El usuario es requerido" else null,
            clave = when {
                s.clave.isBlank() -> "La contraseña es requerida"
                s.clave.length < 4 -> "Debe tener al menos 4 caracteres"
                else -> null
            }
        )
        if (errores.usuario != null || errores.clave != null) {
            _state.update { it.copy(errores = errores) }
            return
        }

        // 2. Validar que las credenciales sean las correctas
        if (s.usuario.trim() != usuarioDemo || s.clave != claveDemo) {
            _state.update {
                it.copy(errores = LoginErrores(credenciales = "Usuario o contraseña incorrectos"))
            }
            return
        }

        // 3. Todo bien: limpia el formulario y avisa
        _state.value = LoginUiState()
        onExito()
    }
}